package com.example.turtledreambackend.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.turtledreambackend.service.posture.PostureDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.RequiredArgsConstructor;
/**
 * 작성자 : 류재영
 * 작성일 : 2025-03-13
 *
 * WebSocket 데이터 <-> AI 서버와 통신
 */
@Component
@RequiredArgsConstructor
public class PostureWebSocketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(PostureWebSocketHandler.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PostureDataService postureDataService;
	private static final String AI_SERVER_HTTP_URL = "http://localhost:8001/analyze-posture";
	private final RestTemplate restTemplate = new RestTemplate();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("웹소켓 연결 성공 - 세션: {}, 원격 주소: {}, 로컬 주소: {}", 
				session.getId(), 
				session.getRemoteAddress(),
				session.getLocalAddress());
		
		// 세션 속성 로깅
		logger.info("세션 속성: {}", session.getAttributes());
		
		// 핸드셰이크 헤더 로깅
		logger.info("핸드셰이크 헤더: {}", session.getHandshakeHeaders());
	}

	/**
	 * WebSocket 클라이언트로부터 텍스트 메시지를 수신하여 처리하는 메서드
	 *
	 * @param session WebSocket 세션 정보
	 * @param message 클라이언트로부터 수신한 메시지 (JSON 형태)
	 * @throws Exception 메시지 처리 중 발생할 수 있는 예외
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		logger.debug("메시지 수신 - 세션: {}, 데이터 크기: {} bytes", session.getId(), payload.length());

		try {
			// JSON 메시지 파싱
			JsonNode jsonNode = objectMapper.readTree(payload);
			String messageType = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

			if ("REGISTER".equals(messageType)) {
				logger.info("REGISTER 메시지 수신 - 세션: {}", session.getId());
				return;
			}

			if ("IMAGE".equals(messageType)) {
				String imageData = jsonNode.has("image") ? jsonNode.get("image").asText() : "";
				String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "anonymous";

				if (imageData.isEmpty()) {
					logger.warn("이미지 데이터가 비어 있습니다. - 세션: {}", session.getId());
					sendErrorResponse(session, "이미지 데이터가 비어 있습니다.");
					return;
				}

				if (!isValidBase64(imageData)) {
					logger.error("잘못된 Base64 이미지 형식 - 세션: {}", session.getId());
					sendErrorResponse(session, "잘못된 Base64 이미지 형식입니다.");
					return;
				}

				// MIME 타입 제거
				String pureBase64 = imageData.replaceFirst("^data:image/\\w+;base64,", "").trim();

				// Base64 인코딩 (정확한 변환을 위해 추가)
				String encodedBase64 = Base64.getEncoder().encodeToString(Base64.getDecoder().decode(pureBase64));

				// AI 서버 요청 데이터 생성
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("Accept", "application/json");

				Map<String, Object> requestBody = new HashMap<>();
				requestBody.put("image", encodedBase64);  // **Base64 인코딩 후 전송**
				requestBody.put("userId", userId);

				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

				try {
					// AI 서버로 POST 요청 전송
					ResponseEntity<String> response = restTemplate.postForEntity(AI_SERVER_HTTP_URL, requestEntity, String.class);

					logger.info("AI 서버 응답 수신 성공 - 응답 크기: {} bytes", response.getBody().length());
					logger.debug("AI 서버 응답 내용: {}", response.getBody());

					// 🔹 AI 응답을 WebSocket 클라이언트에 전송
					session.sendMessage(new TextMessage(response.getBody()));
					logger.info("클라이언트에 AI 서버 응답 전송 완료 - 세션: {}", session.getId());

				} catch (HttpClientErrorException e) {
					logger.error("AI 서버 요청 실패 - 세션: {}, 오류: {}", session.getId(), e.getMessage());

					String errorResponse = e.getResponseBodyAsString();
					logger.error("AI 서버 에러 응답 본문: {}", errorResponse);

					sendErrorResponse(session, "AI 서버 분석 오류 발생");
				}
			} else {
				logger.warn("알 수 없는 메시지 타입: {} - 세션: {}", messageType, session.getId());
				sendErrorResponse(session, "알 수 없는 메시지 타입: " + messageType);
			}
		} catch (Exception e) {
			logger.error("메시지 처리 중 오류 발생 - 세션: {}, 오류: {}", session.getId(), e.getMessage(), e);
			sendErrorResponse(session, "메시지 처리 중 오류 발생: " + e.getMessage());
		}
	}


	/**
	 * Base64 문자열이 유효한 이미지 데이터인지 검증하는 메서드
	 *
	 * @param base64String 검증할 Base64 인코딩된 문자열
	 * @return 유효한 Base64 이미지 데이터이면 true, 그렇지 않으면 false 반환
	 */
	private boolean isValidBase64(String base64String) {
		try {
			String pureBase64 = base64String.replaceFirst("^data:image/\\w+;base64,", "").trim();
			if (pureBase64.isEmpty()) {
				return false;
			}
			byte[] decodedBytes = Base64.getDecoder().decode(pureBase64);
			return decodedBytes.length > 0;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}


	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		logger.info("웹소켓 연결 종료 - 세션: {}, 상태: {}", session.getId(), status);
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		logger.error("웹소켓 전송 오류 - 세션: {}, 오류: {}", session.getId(), exception.getMessage(), exception);
		super.handleTransportError(session, exception);
	}
	
	private void handleAIServerResponse(WebSocketSession session, String response, String userId) {
		try {
			logger.info("AI 서버 응답 처리 시작 - 세션: {}", session.getId());
			
			// 응답 데이터 파싱
			boolean isGoodPosture = processResponseForPosture(response);
			logger.debug("자세 상태: {}", isGoodPosture ? "좋음" : "나쁨");
			
			String postureStatus = extractStatusFromResponse(response);
			logger.debug("자세 상태 문자열: {}", postureStatus);
			
			String feedback = generateFeedbackFromResponse(response);
			logger.debug("피드백 메시지: {}", feedback);
			
			// 메트릭스 데이터 처리
			processMetricsFromResponse(response);
			
			try {
				// 자세 데이터 저장
				postureDataService.savePostureData(userId, isGoodPosture, postureStatus, feedback);
				logger.info("자세 데이터 저장 완료 - 사용자: {}", userId);
			} catch (Exception e) {
				logger.error("자세 데이터 저장 중 오류 발생: {}", e.getMessage(), e);
				// 스택 트레이스 출력
				e.printStackTrace();
				// 데이터 저장 실패해도 클라이언트에게는 응답 전송
			}
			
			// 클라이언트에 응답 전송
			try {
				session.sendMessage(new TextMessage(response));
				logger.info("클라이언트에 응답 전송 완료 - 세션: {}", session.getId());
			} catch (IOException e) {
				logger.error("클라이언트에 응답 전송 중 오류 발생: {}", e.getMessage(), e);
				throw e; // 재발생하여 상위 catch 블록에서 처리
			}
			
		} catch (IOException e) {
			logger.error("응답 처리 중 오류 발생 - 세션: {}, 오류: {}", session.getId(), e.getMessage(), e);
			sendErrorResponse(session, "응답 처리 중 오류 발생: " + e.getMessage());
		}
	}
	
	private void processMetricsFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			
			// 메트릭스 필드 확인
			if (!jsonNode.has("headTilt") && !jsonNode.has("turtleNeckRatio")) {
				logger.warn("응답에 메트릭스 데이터가 없습니다: {}", response);
				return;
			}
			
			// 개별 메트릭스 추출
			double headTilt = 0;
			double turtleNeckRatio = 0;
			
			if (jsonNode.has("headTilt")) {
				headTilt = jsonNode.get("headTilt").asDouble();
			}
			
			if (jsonNode.has("turtleNeckRatio")) {
				turtleNeckRatio = jsonNode.get("turtleNeckRatio").asDouble();
			}
			
			// 메트릭스 데이터 로깅
			logger.debug("자세 메트릭스 파싱 성공 - 머리 기울기: {}, 거북목 비율: {}", headTilt, turtleNeckRatio);
			
		} catch (Exception e) {
			logger.error("메트릭스 처리 중 오류 발생: {}", e.getMessage(), e);
		}
	}
	
	private boolean processResponseForPosture(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			if (jsonNode.has("isGoodPosture")) {
				boolean isGood = jsonNode.get("isGoodPosture").asBoolean();
				logger.debug("자세 상태 파싱 성공: {}", isGood);
				return isGood;
			} else {
				logger.warn("응답에 'isGoodPosture' 필드가 없습니다: {}", response);
				return false;
			}
		} catch (Exception e) {
			logger.error("자세 상태 처리 중 오류: {}", e.getMessage(), e);
			return false;
		}
	}
	
	private String extractStatusFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			if (jsonNode.has("postureStatus")) {
				String status = jsonNode.get("postureStatus").asText();
				logger.debug("자세 상태 문자열 파싱 성공: {}", status);
				return status;
			} else {
				logger.warn("응답에 'postureStatus' 필드가 없습니다: {}", response);
				return "Unknown";
			}
		} catch (Exception e) {
			logger.error("자세 상태 문자열 추출 중 오류: {}", e.getMessage(), e);
			return "Unknown";
		}
	}
	
	private String generateFeedbackFromResponse(String response) {
		try {
			JsonNode jsonNode = objectMapper.readTree(response);
			if (jsonNode.has("feedback")) {
				String feedback = jsonNode.get("feedback").asText();
				logger.debug("피드백 메시지 파싱 성공: {}", feedback);
				return feedback;
			} else {
				logger.warn("응답에 'feedback' 필드가 없습니다: {}", response);
				return "Unable to determine posture.";
			}
		} catch (Exception e) {
			logger.error("피드백 생성 중 오류: {}", e.getMessage(), e);
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
	
	// 이미지 요청을 위한 내부 클래스
	private static class ImageRequest {
		private final String image;
		
		public ImageRequest(String image) {
			this.image = image;
		}
		
		public String getImage() {
			return image;
		}
	}
}