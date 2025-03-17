package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * AI 서버 요청 및 자세 데이터 저장 서비스
 */
@Service
@RequiredArgsConstructor
public class PostureAIService {

    private final RestTemplate restTemplate;

    // 일단 ai 서버 하드코딩 했는데 추후 yml이나 env 파일에서 관리 하면 될 듯
    private static final String AI_SERVER_URL = "http://localhost:8001/analyze-posture";

    public PostureData analyzePosture(String userId, String imageData) {
        // 요청 데이터 생성
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", userId);
        requestData.put("image", imageData);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

        try {
            // AI 서버로 POST 요청 보내기
            ResponseEntity<PostureData> response =
                    restTemplate.exchange(AI_SERVER_URL, HttpMethod.POST, requestEntity, PostureData.class);

            System.out.println("AI 서버 응답: " + response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            System.err.println("AI 서버 요청 실패: " + e.getMessage());
            throw new RuntimeException("AI 서버 요청 실패: " + e.getMessage());
        }
    }
}

