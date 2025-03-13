package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureTrends;
import com.example.turtledreambackend.data.repository.posture.PostureTrendsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * 주간/월간/사용자 지정 범위의 자세 트렌드 분석 API
 */
@RestController
@RequestMapping("/api/posture/trends")
@RequiredArgsConstructor
public class PostureTrendsController {

    private final PostureTrendsRepository postureTrendsRepository;

    /**
     * 특정 사용자의 자세 트렌드 분석 조회
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param periodType 분석할 기간 유형 ("weekly", "monthly", "custom")
     * @return 해당 사용자의 자세 트렌드 데이터
     */
    @GetMapping
    public ResponseEntity<List<PostureTrends>> getTrends(@RequestParam String userId, @RequestParam String periodType) {
        return ResponseEntity.ok(postureTrendsRepository.findByUserIdAndPeriodType(userId, periodType));
    }
}
