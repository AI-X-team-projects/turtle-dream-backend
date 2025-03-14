package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.turtledreambackend.websocket.PostureWebSocketHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-14
 *
 * WebSocket 설정을 위한 파일
 */

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	
	private final PostureWebSocketHandler postureWebSocketHandler;

	/**
	 * WebSocket 핸들러를 등록하는 메서드
	 *
	 * @param registry WebSocket 핸들러를 등록할 레지스트리
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(postureWebSocketHandler, "/ws/posture")
				.setAllowedOrigins("http://localhost:3000")  // 프론트엔드 URL을 명시적으로 지정
				.withSockJS()  // SockJS 지원 추가
				.setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
				.setSessionCookieNeeded(false);  // 세션 쿠키 필요 없음
	}

	/**
	 * WebSocket 메시지 버퍼 크기를 설정하는 Bean
	 *
	 * @return WebSocket 메시지 컨테이너 설정 객체
	 */
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(64 * 1024);  // 10MB
		container.setMaxBinaryMessageBufferSize(64 * 1024);  // 10MB 10485760
		return container;
	}

}
