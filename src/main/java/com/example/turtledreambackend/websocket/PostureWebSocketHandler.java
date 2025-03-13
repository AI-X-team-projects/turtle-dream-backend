package com.example.turtledreambackend.websocket;

import com.example.turtledreambackend.service.posture.PostureDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PostureWebSocketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(PostureWebSocketHandler.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PostureDataService postureDataService;
	private static final String AI_SERVER_WS_URL = "ws://localhost:8001/ws/pose-detection";
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("웹소켓 연결 성공 - 세션: {}", session.getId());
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String imageData = message.getPayload();
		logger.debug("이미지 데이터 수신 - 세션: {}, 데이터 크기: {} bytes", session.getId(), imageData.length());
		
		try {
			ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
			client.execute(URI.create(AI_SERVER_WS_URL), aiSession ->
					aiSession.send(Mono.just(aiSession.textMessage(imageData)))
							.thenMany(aiSession.receive()
									.map(msg -> {
										DataBuffer buffer = msg.getPayload();
										byte[] bytes = new byte[buffer.readableByteCount()];
										buffer.read(bytes);
										return new String(bytes, StandardCharsets.UTF_8);
									}))
							.doOnNext(response -> handleAIServerResponse(session, response))
							.then()
			).block();
		} catch (Exception e) {
			logger.error("AI 서버 연결 실패 - 세션: {}", session.getId(), e);
			sendErrorResponse(session, "AI 서버와의 연결 실패");
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		logger.info("웹소켓 연결 종료 - 세션: {}, 상태: {}", session.getId(), status);
	}
	
	private void handleAIServerResponse(WebSocketSession session, String response) {
		try {
			logger.info("AI 서버 응답 수신 - 세션: {}", session.getId());
			
			boolean isGoodPosture = processResponseForPosture(response);
			String postureStatus = extractStatusFromResponse(response);
			String feedback = generateFeedbackFromResponse(response);
			
			// 메트릭스 데이터 처리
			processMetricsFromResponse(response);
			
			// 자세 데이터 저장 (테스트용 임시 사용자 ID 사용)
			postureDataService.savePostureData("test_user", isGoodPosture, postureStatus, feedback);
			
			// 클라이언트에 응답 전송
			session.sendMessage(new TextMessage(response));
			
		} catch (IOException e) {
			logger.error("응답 처리 중 오류 발생 - 세션: {}", session.getId(), e);
			sendErrorResponse(session, "응답 처리 중 오류 발생");
		}
	}
	
	private void processMetricsFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			JsonNode metrics = jsonNode.get("metrics");
			if (metrics != null) {
				double headTilt = metrics.get("head_tilt").asDouble();
				double turtleNeckRatio = metrics.get("turtle_neck_ratio").asDouble();
				
				// 메트릭스 데이터 로깅만 수행
				logger.debug("자세 메트릭스 - 머리 기울기: {}, 거북목 비율: {}",
						headTilt, turtleNeckRatio);
			}
		} catch (Exception e) {
			logger.error("메트릭스 처리 중 오류 발생", e);
		}
	}
	
	private boolean processResponseForPosture(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("isGoodPosture").asBoolean();
		} catch (Exception e) {
			logger.error("자세 상태 처리 중 오류", e);
			return false;
		}
	}
	
	private String extractStatusFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("postureStatus").asText();
		} catch (Exception e) {
			logger.error("자세 상태 문자열 추출 중 오류", e);
			return "Unknown";
		}
	}
	
	private String generateFeedbackFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("feedback").asText();
		} catch (Exception e) {
			logger.error("피드백 생성 중 오류", e);
			return "Unable to determine posture.";
		}
	}
	
	private void sendErrorResponse(WebSocketSession session, String errorMessage) {
		try {
			String errorResponse = String.format("{\"error\":\"%s\"}", errorMessage);
			session.sendMessage(new TextMessage(errorResponse));
		} catch (IOException e) {
			logger.error("에러 메시지 전송 실패", e);
		}
	}
}