package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.service.posture.PostureAIFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posture/feedback")
public class PostureAIFeedbackController {

    // 작성자: 김태원
    
    private final PostureAIFeedbackService feedbackService;

    // ✅ 일일 피드백
    @GetMapping("/daily/{userId}")
    public ResponseEntity<String> getDailyFeedback(@PathVariable String userId) {
        var data = feedbackService.getTodayPostureData(userId);
        var feedback = feedbackService.generateFeedbackFromList(data, userId);
        return ResponseEntity.ok(feedback);
    }

    // ✅ 월간 피드백
    @GetMapping("/monthly/{userId}")
    public String getMonthlyFeedback(
            @PathVariable String userId,
            @RequestParam(required = false) String startDate, // 날짜가 없으면 null
            @RequestParam(required = false) String endDate
    ) {
        // 문자열 → LocalDate 변환 (없으면 null 유지)
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : null;

        // 서비스 호출 (기본 월간 또는 사용자 지정 기간)
        List<PostureData> dataList = feedbackService.getMonthlyPostureData(userId, start, end);

        // AI 피드백 생성
        return feedbackService.generateFeedbackFromList(dataList, userId);
    }

//    @GetMapping("/test-feedback/{userId}")
//    public ResponseEntity<String> testFeedback(@PathVariable String userId) {
//        String feedback = feedbackService.generateFeedbackWithMockData(userId);
//        return ResponseEntity.ok(feedback);
//    }

}
