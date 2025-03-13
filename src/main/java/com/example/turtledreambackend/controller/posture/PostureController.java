package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.service.posture.PostureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * 사용자 자세 데이터를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/posture")
@RequiredArgsConstructor
public class PostureController {

    private final PostureService postureService;

    /**
     * 특정 날짜의 자세 데이터 조회 (일일 분석)
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 날짜의 자세 데이터 목록
     */
    @GetMapping("/daily")
    public ResponseEntity<List<PostureData>> getDailyPosture(
            @RequestParam String userId,
            @RequestParam(required = false) String date) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        LocalDate localDate = LocalDate.parse(date);
        List<PostureData> data = postureService.getDailyPostureData(userId, localDate);
        return ResponseEntity.ok(data);
    }

    /**
     * 특정 월의 자세 데이터 조회 (월별 분석)
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 자세 데이터 목록
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<PostureData>> getMonthlyPosture(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month) {
        List<PostureData> data = postureService.getMonthlyPostureData(userId, year, month);
        return ResponseEntity.ok(data);
    }
}
