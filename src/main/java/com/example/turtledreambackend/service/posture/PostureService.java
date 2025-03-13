package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
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
    public List<PostureData> getMonthlyPostureData(String userId, int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return postureDataRepository.findByUserIdAndRecordedAtBetween(userId, start, end);
    }
}
