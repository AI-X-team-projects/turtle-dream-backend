package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * AI 서버를 이용한 자세 분석 데이터를 저장하는 서비스
 *
 */
@Service
@RequiredArgsConstructor
public class PostureAIService {

    private final PostureDataRepository postureDataRepository;
    private final RestTemplate restTemplate;

    private static final String AI_SERVER_URL = "ws://localhost:8001/ws/pose-detection";

    /**
     * AI 서버에서 데이터를 받아 MongoDB에 저장
     */
    public PostureData analyzeAndSavePosture(String userId) {
        Map<String, Object> response = restTemplate.getForObject(AI_SERVER_URL, Map.class);

        PostureData postureData = PostureData.builder()
                .userId(userId)
                .isGoodPosture((boolean) response.get("is_good_posture"))
                .postureStatus((String) response.get("posture_status"))
                .feedback((String) response.get("feedback"))
                .badPostureDuration((int) response.get("bad_posture_duration"))
                .totalSessionDuration((int) response.get("total_session_duration"))
                .recordedAt(LocalDateTime.now())
                .build();

        return postureDataRepository.save(postureData);
    }
}
