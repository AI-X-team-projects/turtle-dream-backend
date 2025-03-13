package com.example.turtledreambackend.data.repository;

import com.example.turtledreambackend.data.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * 사용자 정보 저장을 위한 Repository
 *
 */
public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByUsername(String username); // 사용자 아이디를 기준으로 검색
	boolean existsByUsername(String username); // 사용자 이름(username)이 존재하는지 확인하는 메서드
}