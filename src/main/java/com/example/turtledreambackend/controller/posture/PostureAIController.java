package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.service.posture.PostureAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * AI 서버를 이용한 자세 분석 컨트롤러
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class PostureAIController {

    private final PostureAIService postureAIService;

    /**
     * AI 서버에서 자세를 분석하고 결과를 반환
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param requestBody 이미지 데이터 (Base64)
     * @return 분석된 자세 데이터 (PostureData)
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzePosture(@RequestParam String userId, @RequestBody Map<String, Object> requestBody) {
        if (!requestBody.containsKey("image") || requestBody.get("image") == null) {
            return ResponseEntity.badRequest().body("이미지 데이터가 없습니다.");
        }

        try {
            PostureData postureData = postureAIService.analyzePosture(userId, requestBody.get("image").toString());
            return ResponseEntity.ok(postureData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("AI 서버 요청 실패: " + e.getMessage());
        }
    }
}

