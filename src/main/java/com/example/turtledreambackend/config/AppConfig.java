package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * RestTemplate을 Spring 빈으로 등록하는 설정
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
