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
				.setAllowedOrigins("*");  // CORS 허용
	}
}
