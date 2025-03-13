package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // CSRF 비활성화
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", // Swagger 관련 URL
								"/api/user/register", "/api/user/login", "/api/user/logout", "/api/user/check-username", // 회원가입 & 로그인 URL
								"/ws/**", "/ws/posture/**", // WebSocket 엔드포인트
								"/api/posture/**", "/api/ai/**"
						).permitAll() // 인증 없이 접근 허용
						.anyRequest().authenticated() // 나머지 요청은 인증 필요
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				)
				.logout(logout -> logout
						.logoutUrl("/api/user/logout")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) // 리다이렉션 대신 상태 코드 반환
						.permitAll()
				);

		return http.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	// CORS 설정을 Security와 통합
	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("http://localhost:3000"); // 프론트엔드 URL
		corsConfiguration.addAllowedMethod("*"); // 모든 HTTP 메소드 허용 (GET, POST 등)
		corsConfiguration.addAllowedHeader("*"); // 모든 요청 Header 허용
		corsConfiguration.setAllowCredentials(true); // 쿠키 및 인증 정보 허용
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration); // 모든 엔드포인트에 CORS 설정 적용
		
		return source;
	}
}