package com.example.turtledreambackend.websocket;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.turtledreambackend.service.posture.PostureDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

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
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		logger.debug("메시지 수신 - 세션: {}, 데이터 크기: {} bytes", session.getId(), payload.length());
		
		try {
			// JSON 메시지 파싱
			JsonNode jsonNode = objectMapper.readTree(payload);
			String messageType = jsonNode.has("type") ? jsonNode.get("type").asText() : "";
			
			// 메시지 타입에 따른 처리
			if ("REGISTER".equals(messageType)) {
				// 사용자 등록 메시지 처리
				String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "anonymous";
				logger.info("사용자 등록 - 세션: {}, 사용자: {}", session.getId(), userId);
				session.getAttributes().put("userId", userId);
				return;
			} else if ("IMAGE".equals(messageType)) {
				// 이미지 데이터 처리
				String imageData = jsonNode.has("imageData") ? jsonNode.get("imageData").asText() : "";
				String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "anonymous";
				
				if (imageData.isEmpty()) {
					logger.warn("이미지 데이터가 비어있습니다 - 세션: {}", session.getId());
					sendErrorResponse(session, "이미지 데이터가 비어있습니다");
					return;
				}
				
				logger.debug("이미지 데이터 수신 - 세션: {}, 사용자: {}, 데이터 크기: {} bytes", 
						session.getId(), userId, imageData.length());
				
				// 요청 로깅 추가
				logger.info("AI 서버 요청 시작 - URL: {}", AI_SERVER_HTTP_URL);
				
				// HTTP 요청으로 AI 서버에 이미지 데이터 전송
				String response = restTemplate.postForObject(AI_SERVER_HTTP_URL, 
						new ImageRequest(imageData), String.class);
				
				// 응답 로깅 추가
				logger.info("AI 서버 응답 수신 성공 - 응답 크기: {} bytes", response.length());
				logger.debug("AI 서버 응답 내용: {}", response);
				
				// 응답 처리
				handleAIServerResponse(session, response, userId);
			} else {
				logger.warn("알 수 없는 메시지 타입: {} - 세션: {}", messageType, session.getId());
				sendErrorResponse(session, "알 수 없는 메시지 타입: " + messageType);
			}
		} catch (Exception e) {
			logger.error("메시지 처리 중 오류 발생 - 세션: {}, 오류: {}", session.getId(), e.getMessage(), e);
			sendErrorResponse(session, "메시지 처리 중 오류 발생: " + e.getMessage());
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