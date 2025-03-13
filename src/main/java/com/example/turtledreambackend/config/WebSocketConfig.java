package com.example.turtledreambackend.config;

import com.example.turtledreambackend.websocket.PostureWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	
	private final PostureWebSocketHandler postureWebSocketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(postureWebSocketHandler, "/ws/posture")
				.setAllowedOrigins("http://localhost:3000")  // 프론트엔드 도메인 명시
				.withSockJS();  // SockJS 지원 추가 (옵션)
	}
}
