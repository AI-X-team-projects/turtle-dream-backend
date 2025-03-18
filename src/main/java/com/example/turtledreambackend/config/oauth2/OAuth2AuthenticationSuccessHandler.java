package com.example.turtledreambackend.config.oauth2;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * OAuth2 인증 성공 시 처리를 담당하는 핸들러
 * 로그인 성공 후 사용자 정보를 세션에 저장하고 프론트엔드로 리다이렉트하는 역할을 합니다.
 *
 * */

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final String frontendUrl;

    /**
     * 생성자
     * 
     * @param userRepository 사용자 저장소
     * @param frontendUrl 프론트엔드 URL
     */
    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository, 
                                             @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl) {
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
        
        // 기본 리다이렉트 URL 설정
        setDefaultTargetUrl(frontendUrl + "/oauth2/redirect");
        
        log.debug("OAuth2AuthenticationSuccessHandler 생성: frontendUrl={}", frontendUrl);
    }

    /**
     * 인증 성공 시 호출되는 메서드
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보
     * @throws IOException I/O 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // 세션에 사용자 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", attributes);
            
            // 이메일과 이름 정보 추출
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            
            log.debug("OAuth2 로그인 성공: email={}, name={}", email, name);
            
            // 세션에 개별 정보 저장
            if (email != null) {
                session.setAttribute("email", email);
            }
            
            if (name != null) {
                session.setAttribute("name", name);
            }
            
            // 사용자 ID 조회
            String userId = getUserIdByEmail(email);
            if (userId != null) {
                session.setAttribute("userId", userId);
                log.debug("사용자 ID 세션에 저장: userId={}", userId);
            }
            
            // 리다이렉트 URL 생성
            String targetUrl = determineTargetUrl(request, response, authentication, userId);
            
            log.debug("리다이렉트 URL: {}", targetUrl);
            
            // 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    /**
     * 이메일로 사용자 ID를 조회하는 메서드
     * 
     * @param email 이메일
     * @return 사용자 ID
     */
    private String getUserIdByEmail(String email) {
        if (email == null) {
            return null;
        }
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(User::getId).orElse(null);
    }
    
    /**
     * 리다이렉트 URL을 결정하는 메서드
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보
     * @param userId 사용자 ID
     * @return 리다이렉트 URL
     */
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) {
        // 사용자 ID 조회
        String userId = null;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttribute("email");
            userId = getUserIdByEmail(email);
        }
        
        return determineTargetUrl(request, response, authentication, userId);
    }
    
    /**
     * 리다이렉트 URL을 결정하는 메서드 (오버로드)
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보
     * @param userId 사용자 ID
     * @return 리다이렉트 URL
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication, String userId) {
        log.debug("프론트엔드 URL: {}", frontendUrl);
        
        // 프론트엔드 리다이렉트 URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("login", "success");
        
        // 사용자 ID가 있으면 추가
        if (userId != null) {
            builder.queryParam("userId", userId);
        }
        
        // 세션에서 리다이렉트 URI 확인
        HttpSession session = request.getSession(false);
        if (session != null) {
            String redirectUri = (String) session.getAttribute("redirect_uri");
            if (redirectUri != null) {
                log.debug("세션에서 리다이렉트 URI 확인: {}", redirectUri);
                return UriComponentsBuilder.fromUriString(redirectUri)
                        .queryParam("login", "success")
                        .queryParam("userId", userId)
                        .build().toUriString();
            }
        }
        
        String targetUrl = builder.build().toUriString();
        log.debug("최종 리다이렉트 URL: {}", targetUrl);
        
        return targetUrl;
    }
} 