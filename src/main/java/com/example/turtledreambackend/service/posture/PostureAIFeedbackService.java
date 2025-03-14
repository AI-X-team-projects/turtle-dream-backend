package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostureAIFeedbackService {

    private final PostureDataRepository postureDataRepository;
    private final RestTemplate restTemplate;
    private static final Dotenv dotenv = Dotenv.load(); // 자동으로 루트의 .env 파일 읽음
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    // ✅ 환경변수에서 직접 가져오기 (System.getenv)
    private static final String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");

    // ✅ 오늘 하루 데이터 조회
    public List<PostureData> getTodayPostureData(String userId) {
        LocalDate today = LocalDate.now();
        return postureDataRepository.findByUserIdAndRecordedAtBetween(
                userId,
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );
    }

    // ✅ 이번 달 데이터 조회
    public List<PostureData> getMonthlyPostureData(String userId) {
        LocalDate today = LocalDate.now();
        return postureDataRepository.findByUserIdAndRecordedAtBetween(
                userId,
                today.withDayOfMonth(1).atStartOfDay(),
                today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59)
        );
    }

    // ✅ 임시 데이터 기반 피드백 생성 (테스트용)
    public String generateFeedbackWithMockData(String userId) {
        // 임시 데이터 리스트 생성
        List<PostureData> mockDataList = List.of(
                PostureData.builder()
                        .userId(userId)
                        .isGoodPosture(true)
                        .postureStatus("GOOD")
                        .feedback("좋은 자세입니다!")
                        .recordedAt(LocalDateTime.now().minusMinutes(60)) // 60분 전
                        .badPostureDuration(300) // 5분 (300초)
                        .totalSessionDuration(3600) // 1시간 (3600초)
                        .build(),
                PostureData.builder()
                        .userId(userId)
                        .isGoodPosture(false)
                        .postureStatus("BAD")
                        .feedback("허리를 펴세요!")
                        .recordedAt(LocalDateTime.now().minusMinutes(30)) // 30분 전
                        .badPostureDuration(600) // 10분 (600초)
                        .totalSessionDuration(1800) // 30분 (1800초)
                        .build()
        );

        // 기존 피드백 생성 메서드 사용
        return generateFeedbackFromList(mockDataList, userId);
    }

    // ✅ 여러 데이터 기반 피드백 (일간, 월간 공통)
    public String generateFeedbackFromList(List<PostureData> dataList, String userId) {
        if (dataList.isEmpty()) return "해당 기간 동안 기록된 자세 데이터가 없습니다.";

        int totalDuration = dataList.stream().mapToInt(PostureData::getTotalSessionDuration).sum();
        int totalBadDuration = dataList.stream().mapToInt(PostureData::getBadPostureDuration).sum();
        int badPercentage = (int) ((double) totalBadDuration / totalDuration * 100);

        String prompt = String.format("""
역할:
당신은 AI 자세 피드백 전문 코치입니다. 사용자가 제공한 JSON 데이터를 바탕으로 사용자의 자세 습관을 분석하고, 친절하고 따뜻한 피드백을 제공합니다.

목표:
1. 사용자가 자신의 노력을 자랑스럽게 느낄 수 있도록 긍정적이고 진심 어린 피드백 제공
2. 데이터 기반으로 나쁜 자세에 대한 구체적인 분석과 개선 방안 제시
3. 조언에는 반드시 '왜 필요한지' 이유도 포함
4. 너무 기계적이거나 형식적이지 않게, 자연스럽고 따뜻한 말투 사용


아래 JSON 데이터를 분석하여 작성하세요:
{
  "userId": "%s",
  "isGoodPosture": %b,
  "postureStatus": "%s",
  "feedback": "%s",
  "recordedAt": "%s",
  "badPostureDuration": %d,
  "totalSessionDuration": %d
}

규칙:
- 첫 문장은 인사와 함께 오늘의 데이터에 대한 간단한 요약
- 강점과 개선점 모두 포함
- 사용자가 실천할 수 있는 맞춤형 조언 2~3가지 (이유 설명 포함)
- 마지막은 따뜻한 응원으로 마무리
- 피드백은 500 토큰 이내로 짧고 핵심만 담아 작성하세요.
- 너무 장황하지 않게, 핵심적인 정보와 간단한 조언 중심으로 작성하세요.
""",
                dataList.get(0).getUserId(),
                dataList.get(0).isGoodPosture(),
                dataList.get(0).getPostureStatus(),
                dataList.get(0).getFeedback(),
                dataList.get(0).getRecordedAt(),
                dataList.stream().mapToInt(PostureData::getBadPostureDuration).sum(),
                dataList.stream().mapToInt(PostureData::getTotalSessionDuration).sum()
        );
        return callOpenAI(prompt);
    }

    // ✅ OpenAI API 호출
    private String callOpenAI(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 전문적인 자세 코치입니다."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 500
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY); // ✅ 원본 API Key 사용

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        } else {
            return "AI 피드백 생성에 실패했습니다.";
        }
    }
}