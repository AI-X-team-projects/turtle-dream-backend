package com.example.turtledreambackend.data.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 사용자에게 경고 알림을 보낸 이력을 저장하는 엔티티
 *
 * */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "posture_alerts")
public class PostureAlerts extends BaseEntity {

    @Id
    private String id;

    private String userId;         // 사용자 ID
    private LocalDateTime alertTime; // 알림 발생 시간
    private String alertType;       // "BAD_POSTURE", "LONG_SITTING" 등
    private boolean acknowledged;   // 사용자가 알림을 확인했는지 여부
}
