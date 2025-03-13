package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureAlerts;
import com.example.turtledreambackend.data.repository.posture.PostureAlertsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * 자세 알림 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/posture/alerts")
@RequiredArgsConstructor
public class PostureAlertsController {

    private final PostureAlertsRepository postureAlertsRepository;

    /**
     * 특정 사용자의 모든 알림 조회
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @return 해당 사용자의 자세 알림 목록
     */
    @GetMapping
    public ResponseEntity<List<PostureAlerts>> getUserAlerts(@RequestParam String userId) {
        return ResponseEntity.ok(postureAlertsRepository.findByUserId(userId));
    }

    /**
     * 특정 알림을 확인 처리 (acknowledge)
     *
     * @param alertId 알림 ID (PathVariable)
     * @return 상태 코드 200 OK (성공) 또는 404 NOT FOUND (해당 ID의 알림이 없을 경우)
     */
    @PostMapping("/acknowledge/{alertId}")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String alertId) {
        PostureAlerts alert = postureAlertsRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setAcknowledged(true);
            postureAlertsRepository.save(alert);
        }
        return ResponseEntity.ok().build();
    }
}
