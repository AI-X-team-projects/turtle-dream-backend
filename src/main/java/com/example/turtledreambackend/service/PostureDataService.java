package com.example.turtledreambackend.service;

import com.example.turtledreambackend.data.entity.PostureData;
import com.example.turtledreambackend.data.repository.PostureDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


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
