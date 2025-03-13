package com.example.turtledreambackend.data.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 매일/일주일/월간 자세 통계 기록용
 *
 * 포즈 통계 엔티티
 * */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_summary")
public class PostureSummary extends BaseEntity {

    @Id
    private String id;

    private String userId;        // 사용자 ID
    private String summaryDate;   // YYYY-MM-DD or YYYY-MM (날짜 기준)
    private int totalBadPosture;  // 하루 동안의 거북목 횟수
    private int totalSittingTime; // 하루 동안 앉아있던 총 시간 (초)

    private List<String> peakBadPostureTimes; // 거북목이 가장 많이 발생한 시간대 (예: ["10:00", "14:30"])
    private String feedback;  // AI 기반 요약 피드백 메시지
}
