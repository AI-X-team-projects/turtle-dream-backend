package com.example.turtledreambackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	public String sendImageToAIServer(String imageData) {
		String aiServerUrl = "http://localhost:8000/api/pose";  // FastAPI 주소
		return restTemplate.postForObject(aiServerUrl, imageData, String.class);
	}
}

