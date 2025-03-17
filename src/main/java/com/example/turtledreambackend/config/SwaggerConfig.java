package com.example.turtledreambackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 작성자 : 류재영
 * 작성일 : 2025-03-12
 *
 * swagger-ui 설정을 위한 파일
 */

@Configuration
public class SwaggerConfig {
	
	@Bean
	public OpenAPI customOpenAPI() {
		// Security 설정 추가 (세션 기반 방식)
		SecurityScheme securityScheme = new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.COOKIE)
				.name("JSESSIONID")
				.description("서버가 세션 쿠키(JSESSIONID)를 통해 인증을 관리합니다.");
		
		SecurityRequirement securityRequirement = new SecurityRequirement()
				.addList("SessionAuth");
		
		return new OpenAPI()
				.info(apiInfo())
				.addSecurityItem(securityRequirement)
				.components(new io.swagger.v3.oas.models.Components()
						.addSecuritySchemes("SessionAuth", securityScheme));
	}
	
	// Info 객체 정의
	private Info apiInfo() {
		return new Info()
				.title("Turtle-Dream API 문서")
				.description("Turtle-Dream 서비스의 REST API를 Swagger로 테스트할 수 있도록 세션 기반 인증이 적용되었습니다.")
				.version("1.0.0");
	}
}