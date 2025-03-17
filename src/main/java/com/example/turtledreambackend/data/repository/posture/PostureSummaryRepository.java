package com.example.turtledreambackend.data.repository.posture;

import com.example.turtledreambackend.data.entity.posture.PostureSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository  // ✅ 리포지토리 빈 등록
public interface PostureSummaryRepository extends MongoRepository<PostureSummary, String> {
    Optional<PostureSummary> findByUserIdAndSummaryDate(String userId, String date);
    List<PostureSummary> findByUserIdAndSummaryDateStartingWith(String userId, String monthStr);
}
