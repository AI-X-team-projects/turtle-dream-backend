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
 * 한 달 단위의 자세 개선 경향을 분석
 *
 * */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_trends")
public class PostureTrends extends BaseEntity {

    @Id
    private String id;

    private String userId;          // 사용자 ID
    private String month;           // YYYY-MM (월별 데이터)
    private int totalGoodPosture;   // 해당 월의 좋은 자세 횟수
    private int totalBadPosture;    // 해당 월의 나쁜 자세 횟수
    private double improvementRate; // 자세 개선율 (%)
    private String feedback;        // AI 기반 피드백 메시지
}
