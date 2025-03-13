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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PostureWebSocketHandler extends TextWebSocketHandler {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PostureDataService postureDataService;
	private static final String AI_SERVER_WS_URL = "ws://localhost:8001/ws/pose-detection";
	
	// JSON 파싱: 자세 상태(boolean) 추출
	private boolean processResponseForPosture(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("isGoodPosture").asBoolean();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// JSON 파싱: 자세 상태 문자열 추출
	private String extractStatusFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("postureStatus").asText();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown";
		}
	}
	
	// JSON 파싱: 피드백 생성
	private String generateFeedbackFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			return jsonNode.get("feedback").asText();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to determine posture.";
		}
	}
	
	// 사용자 ID 추출 (프론트엔드 URL 파라미터에서 추출)
	private String extractUserIdFromSession(WebSocketSession session) {
		String query = session.getUri().getQuery();
		if (query != null && query.contains("userId=")) {
			return query.split("userId=")[1].split("&")[0];
		}
		return "defaultUser";
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("[INFO] 웹소켓 연결 성공: " + session.getId());
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String imageData = message.getPayload();
		String userId = extractUserIdFromSession(session);
		
		try {
			ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
			client.execute(URI.create(AI_SERVER_WS_URL), aiSession ->
					aiSession.send(Mono.just(aiSession.textMessage(imageData)))
							.thenMany(aiSession.receive()
									.map(msg -> {
										DataBuffer buffer = msg.getPayload();
										byte[] bytes = new byte[buffer.readableByteCount()];
										buffer.read(bytes);  // DataBuffer → byte 배열로 변환
										return new String(bytes, StandardCharsets.UTF_8); // byte 배열 → 문자열 변환
									}))
							.doOnNext(response -> {
								System.out.println("[INFO] AI 서버 응답: " + response);
								
								boolean isGoodPosture = processResponseForPosture(response);
								String postureStatus = extractStatusFromResponse(response);
								String feedback = generateFeedbackFromResponse(response);
								
								postureDataService.savePostureData(userId, isGoodPosture, postureStatus, feedback);
								
								try {
									session.sendMessage(new TextMessage(response));
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							})
							.then()
			).block();
		} catch (Exception e) {
			session.sendMessage(new TextMessage("{\"error\":\"AI 서버와의 연결 실패\"}"));
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("[INFO] 웹소켓 연결 종료: " + session.getId());
	}
}
