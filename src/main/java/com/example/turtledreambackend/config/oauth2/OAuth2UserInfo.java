package com.example.turtledreambackend.config.oauth2;

import java.util.Map;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * OAuth2 사용자 정보를 추상화한 클래스
 * 다양한 OAuth2 제공자(Google, Facebook 등)의 사용자 정보를 일관되게 처리하기 위한 추상 클래스입니다.
 *
 * */

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