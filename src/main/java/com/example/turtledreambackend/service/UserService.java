package com.example.turtledreambackend.service;

import com.example.turtledreambackend.data.dto.UserRequestDTO;
import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	// 회원가입
	public UserResponseDTO register(UserRequestDTO dto) {
		// 중복 체크
		Optional<User> existingUser = userRepository.findByUsername(dto.getUsername());
		if (existingUser.isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 사용자 아이디입니다.");
		}
		
		// User 엔티티 생성 및 저장
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setName(dto.getName());
		user.setGender(dto.getGender());
		user.setAge(dto.getAge());
		user.setHeight(dto.getHeight());
		
		userRepository.save(user);
		
		return toResponseDTO(user);
	}
	
	// 로그인
	public UserResponseDTO login(String username, String password) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}
		
		return toResponseDTO(user);
	}
	
	private UserResponseDTO toResponseDTO(User user) {
		UserResponseDTO dto = new UserResponseDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setName(user.getName());
		dto.setGender(user.getGender());
		dto.setAge(user.getAge());
		dto.setHeight(user.getHeight());
		return dto;
	}
}