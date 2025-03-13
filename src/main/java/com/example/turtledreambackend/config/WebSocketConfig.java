package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.turtledreambackend.websocket.PostureWebSocketHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	
	private final PostureWebSocketHandler postureWebSocketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(postureWebSocketHandler, "/ws/posture")
				.setAllowedOrigins("http://localhost:3000")  // 프론트엔드 URL을 명시적으로 지정
				.withSockJS()  // SockJS 지원 추가
				.setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
				.setSessionCookieNeeded(false);  // 세션 쿠키 필요 없음
	}
}
