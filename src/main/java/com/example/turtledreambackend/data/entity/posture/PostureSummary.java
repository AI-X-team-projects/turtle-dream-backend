package com.example.turtledreambackend.data.entity.posture;

import com.example.turtledreambackend.data.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * 하루, 주간, 월간 자세 통계 기록용
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_summary")
public class PostureSummary extends BaseEntity {

    @Id
    private String id;

    private String userId;        // 사용자 ID
    private String summaryDate;   // YYYY-MM-DD 또는 YYYY-MM (날짜 기준)
    private int totalBadPosture;  // 하루 동안의 나쁜 자세 횟수
    private int totalGoodPosture; // 하루 동안의 좋은 자세 횟수
    private int totalSittingTime; // 하루 동안 총 앉아있던 시간 (초)

    private List<String> peakBadPostureTimes; // 나쁜 자세가 가장 많이 발생한 시간대 (예: ["10:00", "14:30"])
    private String feedback;  // AI 기반 요약 피드백 메시지
}
