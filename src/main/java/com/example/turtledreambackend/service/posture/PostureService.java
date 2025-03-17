package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.entity.posture.PostureSummary;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import com.example.turtledreambackend.data.repository.posture.PostureRepository;
import com.example.turtledreambackend.data.repository.posture.PostureSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 일,월별 자세 데이터 조회를 위한 서비스
 *
 */
@Service
@RequiredArgsConstructor
public class PostureService {

    private final PostureDataRepository postureDataRepository;

    private final PostureRepository postureRepository;

    private final PostureSummaryRepository postureSummaryRepository;

    private final MongoTemplate mongoTemplate;

    /**
     * 일일 자세 데이터 조회
     */
    public List<PostureData> getDailyPostureData(String userId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        LocalDateTime endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

//        System.out.println("조회 범위: " + startOfDay + " ~ " + endOfDay);

        return postureDataRepository.findByUserIdAndRecordedAtBetween(
                userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * 월별 자세 데이터 조회
     */
    public List<PostureData> getPostureDataByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay(); // YYYY-MM-DDT00:00:00
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59); // YYYY-MM-DDT23:59:59

        return postureRepository.findByUserIdAndRecordedAtBetween(userId, startOfDay, endOfDay);
    }

    /**
     *  포즈 데이터를 저장
     */
    public void savePosture(PostureData postureData) {
        // AI 서버에서 recordedAt을 보내준 경우 덮어쓰지 않음
        if (postureData.getRecordedAt() == null) {
            postureData.setRecordedAt(LocalDateTime.now());
        }

//        System.out.println("최종 저장할 recordedAt: " + postureData.getRecordedAt());

        postureDataRepository.save(postureData);
//        System.out.println("AI 분석 데이터 저장 완료: " + postureData);

        // 요약 데이터 업데이트
        updatePostureSummary(postureData);
    }

    /**
     * 하루 동안의 요약 데이터를 업데이트
     */
    private void updatePostureSummary(PostureData postureData) {
        String date = LocalDate.now().toString();
        PostureSummary summary = postureSummaryRepository.findByUserIdAndSummaryDate(postureData.getUserId(), date)
                .orElse(PostureSummary.builder()
                        .userId(postureData.getUserId())
                        .summaryDate(date)
                        .totalBadPosture(0)
                        .totalGoodPosture(0)
                        .build());

        if (postureData.isGoodPosture()) {
            summary.setTotalGoodPosture(summary.getTotalGoodPosture() + 1);
        } else {
            summary.setTotalBadPosture(summary.getTotalBadPosture() + 1);
        }

        postureSummaryRepository.save(summary);
//        System.out.println("자세 요약 데이터 업데이트 완료: " + summary);
    }

    /**
     * 특정 시간대의 나쁜자세 횟수 top3 표시
     */
    public List<Map<String, Object>> getTopBadPostureHours(String userId, String startDate, String endDate) {
        // 1. 날짜 변환 (LocalDateTime -> String)
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");

        // 2. MongoDB Aggregation (시간대별 BAD posture 횟수 집계)
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                                .and("recordedAt").gte(startDateTime).lte(endDateTime)
                                .and("isGoodPosture").is(false) // 나쁜 자세만 필터링
                ),
                Aggregation.project()
                        .andExpression("hour(recordedAt)").as("hour") // recordedAt에서 시간 추출
                        .andInclude("badPostureDuration"),
                Aggregation.group("hour")
                        .sum("badPostureDuration").as("totalBadPostureTime"), // 시간대별 총 badPostureDuration 합산
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalBadPostureTime")), // 많이 나온 순 정렬
                Aggregation.limit(3) // 상위 3개 시간대만 가져옴
        );

        // 3. 결과 반환 (List<Document>로 변환)
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "posture_data", Document.class);

        List<Map<String, Object>> outputList = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            // 🔥 _id(시간 필드) 처리: Integer, String, ObjectId 등 다양한 타입 가능
            String hour;
            Object hourObj = doc.get("_id");  // _id 필드 가져오기
            if (hourObj instanceof Integer) {
                hour = hourObj + ":00";  // 정수면 그대로 사용
            } else if (hourObj instanceof String) {
                hour = hourObj + ":00";  // 문자열이면 그대로 사용
            } else {
                hour = "Unknown";  // 변환 불가하면 기본값
            }

            // 🔥 totalBadPostureTime 처리 (숫자 변환)
            Object countObj = doc.get("totalBadPostureTime");
            int count = 0;
            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            }

            // 🔥 변환된 데이터 저장
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("hour", hour);
            resultMap.put("count", count);
            outputList.add(resultMap);
        }

        return outputList;
    }

}
