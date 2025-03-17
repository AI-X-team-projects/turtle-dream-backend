package com.example.turtledreambackend.data.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.turtledreambackend.data.entity.User;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 수정자 : 
 * 수정일 : 2025-03-14
 * 수정이유 : OAuth2 로그인 지원을 위한 메서드 추가
 *
 * 사용자 정보 저장을 위한 Repository
 *
 */
public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByUsername(String username); // 사용자 아이디를 기준으로 검색
	boolean existsByUsername(String username); // 사용자 이름(username)이 존재하는지 확인하는 메서드
	
	// OAuth2 관련 메서드
	Optional<User> findByEmail(String email); // 이메일을 기준으로 검색
	Optional<User> findByProviderAndProviderId(String provider, String providerId); // 소셜 로그인 제공자와 ID를 기준으로 검색
	boolean existsByEmail(String email); // 이메일이 존재하는지 확인하는 메서드
}