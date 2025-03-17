package com.example.turtledreambackend.service.posture;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;

import lombok.RequiredArgsConstructor;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 자세 데이타를 저장하기 위한 서비스
 *
 */
@Service
@RequiredArgsConstructor
public class PostureDataService {
	
	private final PostureDataRepository repository;
	
	public void savePostureData(String userId, boolean isGoodPosture, String postureStatus, String feedback) {
		try {
			PostureData data = PostureData.builder()
					.userId(userId)
					.isGoodPosture(isGoodPosture)
					.postureStatus(postureStatus)
					.feedback(feedback)
					.recordedAt(LocalDateTime.now())
					.badPostureDuration(0)
					.totalSessionDuration(0)
					.build();
			
			PostureData savedData = repository.save(data);
//			System.out.println("자세 데이터 저장 성공: " + savedData.getId());
		} catch (Exception e) {
			System.err.println("자세 데이터 저장 실패: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
