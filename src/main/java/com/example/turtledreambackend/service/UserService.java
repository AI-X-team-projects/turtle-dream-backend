package com.example.turtledreambackend.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.turtledreambackend.data.dto.LoginRequestDTO;
import com.example.turtledreambackend.data.dto.UserRequestDTO;
import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.data.entity.User;
import com.example.turtledreambackend.data.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 관련 서비스
 */
@RequiredArgsConstructor
@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	/**
	 * 아이디 중복 확인
	 * 
	 * @param username 사용자 아이디
	 * @return 사용 가능 여부
	 */
	public boolean isUsernameAvailable(String username) {
		return !userRepository.existsByUsername(username); // 해당 username이 존재하지 않아야 true 반환
	}
	
	/**
	 * 회원가입 (기존 메서드)
	 * 
	 * @param dto 회원가입 요청 DTO
	 * @return 회원가입 결과
	 */
	public UserResponseDTO register(UserRequestDTO dto) {
		// 중복 체크
		if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 사용자 아이디입니다.");
		}
		
		// Builder 패턴을 통한 객체 생성
		User user = User.builder()
				.username(dto.getUsername())
				.password(passwordEncoder.encode(dto.getPassword()))
				.name(dto.getName())
				.gender(dto.getGender())
				.age(dto.getAge())
				.height(dto.getHeight())
				.build();
		
		userRepository.save(user);
		
		return toResponseDTO(user);
	}
	
	/**
	 * 회원가입 (새 메서드)
	 * 
	 * @param dto 회원가입 요청 DTO
	 * @return 회원가입 결과
	 */
	public UserResponseDTO createUser(UserRequestDTO dto) {
		// 중복 체크
		if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 사용자 아이디입니다.");
		}
		
		// Builder 패턴을 통한 객체 생성
		User user = User.builder()
				.id(UUID.randomUUID().toString())
				.username(dto.getUsername())
				.password(passwordEncoder.encode(dto.getPassword()))
				.name(dto.getName())
				.gender(dto.getGender())
				.age(dto.getAge())
				.height(dto.getHeight())
				.createdAt(Instant.now())
				.lastLogin(Instant.now())
				.build();
		
		userRepository.save(user);
		
		return toResponseDTO(user);
	}
	
	/**
	 * 로그인 (기존 메서드)
	 * 
	 * @param username 사용자 아이디
	 * @param password 비밀번호
	 * @return 로그인 결과
	 */
	public UserResponseDTO login(String username, String password) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		// 로그인 성공시 last_login update
		user.setLastLogin(Instant.now());
		userRepository.save(user);
		
		return toResponseDTO(user);
	}
	
	/**
	 * 로그인 (새 메서드)
	 * 
	 * @param loginRequestDTO 로그인 요청 DTO
	 * @return 로그인 결과
	 */
	public UserResponseDTO login(LoginRequestDTO loginRequestDTO) {
		return login(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
	}
	
	/**
	 * Entity → DTO 변환 메서드
	 * 
	 * @param user 사용자 엔티티
	 * @return 사용자 응답 DTO
	 */
	private UserResponseDTO toResponseDTO(User user) {
		return UserResponseDTO.builder()
				.id(user.getId())
				.username(user.getUsername())
				.name(user.getName())
				.gender(user.getGender())
				.age(user.getAge())
				.height(user.getHeight())
				.build();
	}
}
