package com.example.turtledreambackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

/**
 * 환경 변수를 로드하는 설정 클래스
 */
@Configuration
public class EnvConfig {

    @Autowired
    private Environment env;
    
    private Dotenv dotenv;

    /**
     * .env 파일에서 환경 변수를 로드하는 Dotenv 빈을 생성
     * 
     * @return Dotenv 객체
     */
    @Bean
    public Dotenv dotenv() {
        this.dotenv = Dotenv.configure()
                .ignoreIfMissing() // .env 파일이 없어도 무시
                .load();
        return this.dotenv;
    }
    
    /**
     * 환경 변수를 시스템 프로퍼티로 설정
     */
    @PostConstruct
    public void init() {
        if (this.dotenv == null) {
            this.dotenv = dotenv();
        }
        
        // Google OAuth2 설정
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.client-name", dotenv.get("GOOGLE_CLIENT_NAME"));
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.client-id", dotenv.get("GOOGLE_CLIENT_ID"));
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.client-secret", dotenv.get("GOOGLE_CLIENT_SECRET"));
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.redirect-uri", dotenv.get("GOOGLE_REDIRECT_URI"));
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.authorization-grant-type", dotenv.get("GOOGLE_AUTHORIZATION_GRANT_TYPE"));
        setSystemPropertyIfNotExists("spring.security.oauth2.client.registration.google.scope", dotenv.get("GOOGLE_SCOPE"));
    }
    
    /**
     * 시스템 프로퍼티가 없는 경우에만 설정
     * 
     * @param key 프로퍼티 키
     * @param value 프로퍼티 값
     */
    private void setSystemPropertyIfNotExists(String key, String value) {
        if (System.getProperty(key) == null && value != null) {
            System.setProperty(key, value);
        }
    }
} 