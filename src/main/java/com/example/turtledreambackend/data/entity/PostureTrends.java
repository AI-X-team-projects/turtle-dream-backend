package com.example.turtledreambackend.data.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 주간 & 월간 자세 개선 경향을 분석하고,
 * 사용자가 직접 설정한 날짜 범위에 대한 분석도 제공
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_trends")
public class PostureTrends extends BaseEntity {

    @Id
    private String id;

    private String userId;            // 사용자 ID
    private String periodType;        // "weekly", "monthly", "custom" (주간, 월간, 사용자 지정)
    private String startDate;         // 분석 시작 날짜 (YYYY-MM-DD)
    private String endDate;           // 분석 종료 날짜 (YYYY-MM-DD)

    private int totalGoodPosture;     // 해당 기간의 좋은 자세 횟수
    private int totalBadPosture;      // 해당 기간의 나쁜 자세 횟수
    private double improvementRate;   // 자세 개선율 (%)
    private String feedback;          // AI 기반 피드백 메시지
}
