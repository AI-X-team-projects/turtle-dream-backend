package com.example.turtledreambackend.data.repository.posture;

import com.example.turtledreambackend.data.entity.posture.PostureTrends;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 주간/월간/사용자 지정 날짜별 자세 트렌드 조회를 위한 Repository
 *
 */
public interface PostureTrendsRepository extends MongoRepository<PostureTrends, String> {
    List<PostureTrends> findByUserIdAndPeriodType(String userId, String periodType);

    @Query("{ 'userId': ?0, 'startDate': { $gte: ?1 }, 'endDate': { $lte: ?2 } }")
    List<PostureTrends> findByUserIdAndDateRange(String userId, String startDate, String endDate);
}
