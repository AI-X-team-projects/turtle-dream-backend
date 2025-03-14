package com.example.turtledreambackend.data.repository.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostureRepository extends MongoRepository<PostureData, String> {
    List<PostureData> findByUserIdAndRecordedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
}
