package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.service.posture.PostureAIFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posture/feedback")
public class PostureAIFeedbackController {

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
    public ResponseEntity<String> getMonthlyFeedback(@PathVariable String userId) {
        var data = feedbackService.getMonthlyPostureData(userId);
        var feedback = feedbackService.generateFeedbackFromList(data, userId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/test-feedback/{userId}")
    public ResponseEntity<String> testFeedback(@PathVariable String userId) {
        String feedback = feedbackService.generateFeedbackWithMockData(userId);
        return ResponseEntity.ok(feedback);
    }

}
