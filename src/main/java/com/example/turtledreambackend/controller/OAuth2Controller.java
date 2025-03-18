package com.example.turtledreambackend.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * OAuth2 인증 요청을 처리하는 컨트롤러
 * 구글 등의 OAuth2 제공자로 인증 요청을 리다이렉트하는 엔드포인트를 제공합니다.
 *
 * */

@RestController
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 현재 로그인한 사용자 정보를 반환하는 엔드포인트
     * 
     * @param oAuth2User OAuth2 사용자 정보
     * @param request HTTP 요청
     * @return 사용자 정보
     */
    @GetMapping("/api/oauth2/user")
    public ResponseEntity<UserResponseDTO> getUser(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletRequest request) {
        // 세션에서 사용자 정보 조회
        HttpSession session = request.getSession(false);
        UserResponseDTO responseDTO = new UserResponseDTO();
        
        if (session == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setMessage("세션이 존재하지 않습니다.");
            return ResponseEntity.ok(responseDTO);
        }
        
        // 세션에서 사용자 ID 조회
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setMessage("사용자 ID가 세션에 존재하지 않습니다.");
            return ResponseEntity.ok(responseDTO);
        }
        
        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setMessage("사용자 정보를 찾을 수 없습니다.");
            return ResponseEntity.ok(responseDTO);
        }
        
        // 응답 데이터 구성
        responseDTO.setAuthenticated(true);
        responseDTO.setUserId(user.getId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setName(user.getName());
        responseDTO.setEmail(user.getEmail());
        
        return ResponseEntity.ok(responseDTO);
    }
    
    /**
     * 세션에 저장된 사용자 정보를 반환하는 엔드포인트
     * 
     * @param request HTTP 요청
     * @return 세션에 저장된 사용자 정보
     */
    @GetMapping("/api/oauth2/session")
    public ResponseEntity<Map<String, Object>> getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }
        
        Object user = session.getAttribute("user");
        String userId = (String) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");
        String name = (String) session.getAttribute("name");
        
        if (user == null) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }
        
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("authenticated", true);
        sessionInfo.put("user", user);
        sessionInfo.put("userId", userId);
        sessionInfo.put("email", email);
        sessionInfo.put("name", name);
        
        return ResponseEntity.ok(sessionInfo);
    }
    
    /**
     * 현재 로그인한 사용자 정보를 UserResponseDTO 형식으로 반환하는 엔드포인트
     * (기존 로그인 API와 동일한 형식)
     * 
     * @param request HTTP 요청
     * @return 사용자 정보 DTO
     */
    @GetMapping("/api/oauth2/current-user")
    public ResponseEntity<UserResponseDTO> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return ResponseEntity.ok(null);
        }
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(null);
        }
        
        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(null);
        }
        
        // UserResponseDTO 생성
        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .gender(user.getGender())
                .age(user.getAge())
                .height(user.getHeight())
                .build();
        
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * OAuth2 인증 요청을 처리하는 메서드
     * 
     * @param provider OAuth2 제공자 (google, facebook 등)
     * @param redirectUri 리다이렉트 URI
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 리다이렉트 뷰
     * @throws IOException I/O 예외 발생 시
     */
    @GetMapping("/oauth2/authorize/{provider}")
    public RedirectView authorize(
            @PathVariable String provider,
            @RequestParam(required = false) String redirectUri,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.debug("OAuth2 인증 요청: provider={}, redirectUri={}", provider, redirectUri);
        
        // 세션에 리다이렉트 URI 저장
        if (redirectUri != null) {
            request.getSession().setAttribute("redirect_uri", redirectUri);
        } else {
            // 기본 리다이렉트 URI 설정
            redirectUri = frontendUrl + "/oauth2/redirect";
            request.getSession().setAttribute("redirect_uri", redirectUri);
        }
        
        // 상태(state) 생성
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        // 세션에 상태(state) 저장
        request.getSession().setAttribute("oauth2_state", state);
        
        if ("google".equals(provider)) {
            // 구글 OAuth2 인증 URL 생성
            String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&response_type=code" +
                    "&scope=profile%20email" +
                    "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8);
            
            log.debug("구글 OAuth2 인증 URL: {}", authorizationUri);
            
            // 리다이렉트
            return new RedirectView(authorizationUri);
        } else {
            // 지원하지 않는 제공자
            log.error("지원하지 않는 OAuth2 제공자: {}", provider);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
            return null;
        }
    }
} 