package com.example.turtledreambackend.data.repository.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 사용자별 자세 데이터를 저장하기 위한 Repository
 *
 */
public interface PostureDataRepository extends MongoRepository<PostureData, String> {
	List<PostureData> findByUserId(String userId);
	List<PostureData> findByUserIdAndRecordedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
}
