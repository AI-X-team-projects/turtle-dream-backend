package com.example.turtledreambackend.config.oauth2;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * OAuth2 인증 실패 시 처리를 담당하는 핸들러
 * 로그인 실패 시 오류 메시지와 함께 로그인 페이지로 리다이렉트하는 역할을 합니다.
 *
 * */

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    /**
     * 인증 실패 시 호출되는 메서드
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param exception 인증 예외
     * @throws IOException I/O 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 실패 메시지
        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            errorMessage = "인증에 실패했습니다.";
        }
        
        // 리다이렉트 URL 생성
        String targetUrl = "/login";
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", errorMessage)
                .build().toUriString();
        
        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
} 