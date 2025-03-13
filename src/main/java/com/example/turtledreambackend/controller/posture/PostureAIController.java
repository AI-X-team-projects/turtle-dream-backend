package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.service.posture.PostureAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @return 분석된 자세 데이터 (PostureData)
     */
    @PostMapping("/analyze")
    public ResponseEntity<PostureData> analyzePosture(@RequestParam String userId) {
        PostureData postureData = postureAIService.analyzeAndSavePosture(userId);
        return ResponseEntity.ok(postureData);
    }
}
