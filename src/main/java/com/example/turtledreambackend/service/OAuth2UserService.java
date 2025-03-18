package com.example.turtledreambackend.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.turtledreambackend.config.oauth2.OAuth2UserInfo;
import com.example.turtledreambackend.config.oauth2.OAuth2UserInfoFactory;
import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-17
 *
 * OAuth2 사용자 서비스
 * OAuth2 인증 과정에서 사용자 정보를 로드하고 처리하는 역할을 합니다.
 *
 * */

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 인증 후 사용자 정보를 로드하는 메서드
     * 
     * @param userRequest OAuth2 사용자 요청 정보
     * @return OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 오류 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * OAuth2 사용자 정보를 처리하는 메서드
     * 
     * @param userRequest OAuth2 사용자 요청 정보
     * @param oAuth2User OAuth2 사용자 정보
     * @return OAuth2User 객체
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 소셜 로그인 제공자 이름 (google, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // 사용자 속성 맵
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // OAuth2UserInfo 객체 생성
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        
        // 이메일 검증
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("이메일을 찾을 수 없습니다.");
        }
        
        // 사용자 조회 또는 생성
        User user = findOrCreateUser(oAuth2UserInfo);
        
        // 세션에 사용자 ID 저장
        if (user != null && RequestContextHolder.getRequestAttributes() != null) {
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("name", user.getName());
        }
        
        // 사용자 정보를 속성에 추가
        Map<String, Object> userAttributes = new HashMap<>(attributes);
        userAttributes.put("id", user.getId());
        userAttributes.put("username", user.getUsername());
        userAttributes.put("name", user.getName());
        userAttributes.put("email", user.getEmail());
        
        // DefaultOAuth2User 객체 생성 및 반환
        return new DefaultOAuth2User(
                Collections.emptyList(),
                userAttributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    /**
     * 사용자 조회 또는 생성하는 메서드
     * 
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return User 객체
     */
    private User findOrCreateUser(OAuth2UserInfo oAuth2UserInfo) {
        // 소셜 로그인 제공자와 ID로 사용자 조회
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                oAuth2UserInfo.getProvider(),
                oAuth2UserInfo.getId()
        );
        
        User user;
        
        if (userOptional.isPresent()) {
            // 기존 사용자가 있는 경우 정보 업데이트
            user = userOptional.get();
            user.updateFromOAuth2UserInfo(oAuth2UserInfo);
            return userRepository.save(user);
        } else {
            // 이메일로 사용자 조회
            Optional<User> emailUser = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            
            if (emailUser.isPresent()) {
                // 이메일이 같은 사용자가 있는 경우 소셜 로그인 정보 업데이트
                user = emailUser.get();
                user.setProvider(oAuth2UserInfo.getProvider());
                user.setProviderId(oAuth2UserInfo.getId());
                user.updateFromOAuth2UserInfo(oAuth2UserInfo);
                return userRepository.save(user);
            } else {
                // 새 사용자 생성
                user = User.createFromOAuth2UserInfo(oAuth2UserInfo);
                return userRepository.save(user);
            }
        }
    }
} 