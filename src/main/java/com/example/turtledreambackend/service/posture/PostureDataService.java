package com.example.turtledreambackend.service.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
		PostureData data = PostureData.builder()
				.userId(userId)
				.isGoodPosture(isGoodPosture)
				.postureStatus(postureStatus)
				.feedback(feedback)
				.build();
		
		repository.save(data);
	}
}
