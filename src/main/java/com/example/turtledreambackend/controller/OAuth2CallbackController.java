package com.example.turtledreambackend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 콜백을 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2CallbackController {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * 구글 OAuth2 콜백을 처리하는 메서드
     * 
     * @param code 인증 코드
     * @param state 상태 값
     * @param request HTTP 요청
     * @return 사용자 정보
     */
    @GetMapping("/callback/google")
    public ResponseEntity<Map<String, Object>> handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request) {
        
        log.debug("Google OAuth2 callback received. Code: {}, State: {}", code, state);
        
        // 세션에서 사용자 정보 조회
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();
        
        if (session == null) {
            response.put("authenticated", false);
            response.put("message", "세션이 존재하지 않습니다.");
            return ResponseEntity.ok(response);
        }
        
        // 세션에서 사용자 ID 조회
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            response.put("authenticated", false);
            response.put("message", "사용자 ID가 세션에 존재하지 않습니다.");
            return ResponseEntity.ok(response);
        }
        
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            response.put("authenticated", false);
            response.put("message", "사용자 정보를 찾을 수 없습니다.");
            return ResponseEntity.ok(response);
        }
        
        User user = userOptional.get();
        
        // 응답 데이터 구성
        response.put("authenticated", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        
        // 인증 토큰 정보 추가 (인증 토큰이 있는 경우)
        if (request.getUserPrincipal() instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) request.getUserPrincipal();
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(), 
                    oauthToken.getName());
            
            if (client != null) {
                OAuth2AccessToken accessToken = client.getAccessToken();
                response.put("tokenType", accessToken.getTokenType().getValue());
                response.put("tokenValue", accessToken.getTokenValue());
                response.put("tokenExpiry", accessToken.getExpiresAt());
            }
        }
        
        return ResponseEntity.ok(response);
    }
} 