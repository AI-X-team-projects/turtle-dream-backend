package com.example.turtledreambackend.data.entity.posture;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    private String userId;        // 사용자 ID
    private LocalDateTime startTime; // 세션 시작 시간
    private LocalDateTime endTime;   // 세션 종료 시간
}
