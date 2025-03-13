package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // CSRF 비활성화
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", // Swagger 관련 URL
								"/api/user/register", "/api/user/login", "/api/user/check-username", // 회원가입 & 로그인 URL
								"/ws/**", "/ws/posture/**" // WebSocket 엔드포인트
						).permitAll() // 인증 없이 접근 허용
						.anyRequest().authenticated() // 나머지 요청은 인증 필요
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				)
				.logout(logout -> logout
						.logoutUrl("/api/user/logout")
						.logoutSuccessUrl("/api/user/login")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
				);
		
		return http.build();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("http://localhost:3000");
		corsConfiguration.addAllowedMethod("*");
		corsConfiguration.addAllowedHeader("*");
		corsConfiguration.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		
		return new CorsFilter(source);
	}
}