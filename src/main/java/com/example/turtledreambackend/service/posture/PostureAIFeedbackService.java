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
                당신은 AI 자세 교정 피드백 코치입니다. 사용자가 제공하는 JSON 데이터를 바탕으로, 사용자의 자세 데이터를 분석하고 친절하고 동기부여가 되는 피드백을 제공합니다. 
                사용자가 나쁜 자세를 얼마나 오래 유지했는지, 세션 동안의 총 시간 등을 분석하여 칭찬, 경고, 조언을 함께 포함한 자연스러운 말투로 피드백을 작성하세요. 
                너무 기계적이지 않으며, 실제 코치처럼 따뜻하고 동기부여를 줄 수 있는 톤을 사용하세요.

                목표:
                1. 데이터를 바탕으로 정확하고 간결한 피드백 제공
                2. 사용자에게 동기부여 및 개선 방향 제시
                3. 피드백 구성: (1) 인사와 간단한 요약 (2) 데이터 기반 피드백 (3) 맞춤형 조언 (4) 응원 마무리

                출력 형식 예시:

                ## 🐢 거북이의 꿈 - %s 자세 피드백

                안녕하세요, %s님! 바른 자세를 위해 노력해주셔서 정말 멋져요. 🙌

                - ⏱️ 총 교정 시간: %s
                - ⚠️ 나쁜 자세 유지 시간: %s

                ### 📊 분석 및 피드백
                이번 기간 동안 약 %d%%의 시간이 나쁜 자세로 기록되었습니다. 장시간 나쁜 자세는 목과 어깨에 무리를 줄 수 있어요. 한 번쯤 자리에서 일어나 어깨를 쭉 펴보는 건 어떨까요?

                ### 🌱 추천 팁
                - 거북목 스트레칭 3분
                - 어깨 돌리기 10회
                - 20분마다 바른 자세 체크 알림 설정

                오늘도 작은 실천이 큰 변화를 만듭니다. 화이팅입니다! 💚
                """, userId, userId, totalDuration / 60, totalBadDuration / 60, badPercentage);

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