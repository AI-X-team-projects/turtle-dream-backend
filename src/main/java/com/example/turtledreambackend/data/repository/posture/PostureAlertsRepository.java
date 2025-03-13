package com.example.turtledreambackend.data.repository.posture;

import com.example.turtledreambackend.data.entity.posture.PostureAlerts;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 자세 알림 관련 사용자 아이디 가져오는 Repository
 *
 */
public interface PostureAlertsRepository extends MongoRepository<PostureAlerts, String> {
    List<PostureAlerts> findByUserId(String userId);
}
