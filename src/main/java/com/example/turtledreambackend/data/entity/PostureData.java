package com.example.turtledreambackend.data.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_data")
public class PostureData extends BaseEntity {
	
	@Id
	private String id;
	
	private boolean isGoodPosture;   // 좋은 자세 여부
	private String userId;           // 사용자 ID
	private String postureStatus;    // 자세 상태 (올바른 자세/거북목/구부정 등)
	private String feedback;         // 피드백 메시지
}

