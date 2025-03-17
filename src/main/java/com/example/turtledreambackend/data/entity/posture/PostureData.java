package com.example.turtledreambackend.data.entity.posture;

import com.example.turtledreambackend.data.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-12
 *
 * 수정자 : 김동규
 * 수정일 : 2025-03-13
 * 수정이유 : 포즈 데이터 추가
 * recordedAt, badPostureDuration, totalSessionDuration
 *
 * 포즈 엔티티
 * */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_data")
public class PostureData extends BaseEntity {

	@Id
	private String id;

	private String userId;           // 사용자 ID
	private boolean isGoodPosture;   // 좋은 자세 여부
	private String postureStatus;    // 자세 상태 (GOOD, BAD, SLOUCH 등)
	private String feedback;         // AI 피드백 메시지

	private LocalDateTime recordedAt;  // 자세 기록 시간
	private int badPostureDuration;    // 나쁜 자세 유지 시간 (초 단위)
	private int totalSessionDuration;  // 전체 앉아있던 시간 (초 단위)
}

