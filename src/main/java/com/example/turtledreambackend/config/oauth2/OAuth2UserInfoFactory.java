package com.example.turtledreambackend.config.oauth2;

import java.util.Map;

/**
 * OAuth2 제공자에 따라 적절한 OAuth2UserInfo 구현체를 생성하는 팩토리 클래스
 */
public class OAuth2UserInfoFactory {
    
    /**
     * 제공자 이름과 속성 맵을 기반으로 적절한 OAuth2UserInfo 구현체를 생성
     * 
     * @param registrationId 소셜 로그인 제공자 이름 (google, naver 등)
     * @param attributes 사용자 속성 맵
     * @return OAuth2UserInfo 구현체
     * @throws IllegalArgumentException 지원하지 않는 제공자인 경우
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }
    }
} 