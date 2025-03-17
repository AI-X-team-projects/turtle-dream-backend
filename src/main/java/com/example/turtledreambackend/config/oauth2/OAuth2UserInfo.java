package com.example.turtledreambackend.config.oauth2;

import java.util.Map;

/**
 * 소셜 로그인 제공자로부터 받은 사용자 정보를 표준화하기 위한 인터페이스
 */
public interface OAuth2UserInfo {
    
    /**
     * 소셜 로그인 제공자의 ID를 반환
     * @return 소셜 로그인 제공자의 ID
     */
    String getId();
    
    /**
     * 소셜 로그인 제공자 이름을 반환 (google, naver 등)
     * @return 소셜 로그인 제공자 이름
     */
    String getProvider();
    
    /**
     * 사용자 이메일을 반환
     * @return 사용자 이메일
     */
    String getEmail();
    
    /**
     * 사용자 이름을 반환
     * @return 사용자 이름
     */
    String getName();
    
    /**
     * 사용자 프로필 이미지 URL을 반환
     * @return 사용자 프로필 이미지 URL
     */
    String getImageUrl();
    
    /**
     * 원본 속성 맵을 반환
     * @return 원본 속성 맵
     */
    Map<String, Object> getAttributes();
} 