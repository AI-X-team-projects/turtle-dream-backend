package com.example.turtledreambackend.data.repository;

import com.example.turtledreambackend.data.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByUsername(String username); // 사용자 아이디를 기준으로 검색
}