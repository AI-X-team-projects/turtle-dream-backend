package com.example.turtledreambackend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.turtledreambackend.config.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.turtledreambackend.service.OAuth2UserService;

import lombok.RequiredArgsConstructor;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * 보안 설정 클래스
 * OAuth2 로그인을 포함한 보안 관련 설정을 정의합니다.
 *
 * */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final OAuth2UserService oAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	/**
	 * 비밀번호 인코더 빈
	 * 
	 * @return BCryptPasswordEncoder
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 보안 필터 체인 설정
	 * 
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 * @throws Exception 예외 발생 시
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(
							"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", // Swagger 관련 URL
							"/api/user/register", "/api/user/login", "/api/user/logout", "/api/user/check-username", // 회원가입 & 로그인 URL
							"/ws/**", "/ws/posture/**", // WebSocket 엔드포인트
							"/api/posture/**", "/api/ai/**","https://api.openai.com/v1/chat/completions",
							"/", "/error", "/main", "/api/user/**","/oauth2/**","/api/oauth2/**",
							"/login/**"
					).permitAll() // 인증 없이 접근 허용
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(oAuth2UserService)
				)
				.successHandler(oAuth2AuthenticationSuccessHandler)
			);

		return http.build();
	}
	
	/**
	 * CORS 설정
	 * 
	 * @return CorsConfigurationSource
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 프론트엔드 URL
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true); // 쿠키 및 인증 정보 허용
		configuration.setMaxAge(3600L); // 1시간 동안 preflight 요청 캐싱
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}