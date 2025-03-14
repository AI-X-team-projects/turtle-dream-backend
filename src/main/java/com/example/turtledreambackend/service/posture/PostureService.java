package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.entity.posture.PostureSummary;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import com.example.turtledreambackend.data.repository.posture.PostureRepository;
import com.example.turtledreambackend.data.repository.posture.PostureSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 일일 자세 데이터 조회
     */
    public List<PostureData> getDailyPostureData(String userId, LocalDate date) {
        return postureDataRepository.findByUserIdAndRecordedAtBetween(
                userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * 월별 자세 데이터 조회
     */
//    public List<PostureData> getMonthlyPostureData(String userId, int year, int month) {
//        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
//        LocalDateTime end = start.plusMonths(1);
//        return postureDataRepository.findByUserIdAndRecordedAtBetween(userId, start, end);
//    }

    public List<PostureSummary> getMonthlyPostureData(String userId, int year, int month) {
        String monthStr = String.format("%04d-%02d", year, month);
        return postureSummaryRepository.findByUserIdAndSummaryDateStartingWith(userId, monthStr);
    }


//    public void savePosture(PostureData postureData) {
//        postureData.setRecordedAt(LocalDateTime.now());  // 현재 시간 설정
//        postureDataRepository.save(postureData);
//        System.out.println("AI 분석 데이터 저장 완료: " + postureData);
//    }

    /**
     *  포즈 데이터를 저장
     */
    public void savePosture(PostureData postureData) {
        // AI 서버에서 recordedAt을 보내준 경우 덮어쓰지 않음
        if (postureData.getRecordedAt() == null) {
            postureData.setRecordedAt(LocalDateTime.now());
        }

        System.out.println("최종 저장할 recordedAt: " + postureData.getRecordedAt());

        postureDataRepository.save(postureData);
        System.out.println("AI 분석 데이터 저장 완료: " + postureData);

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
        System.out.println("자세 요약 데이터 업데이트 완료: " + summary);
    }
}
