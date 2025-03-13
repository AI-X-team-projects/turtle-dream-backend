package com.example.turtledreambackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.turtledreambackend.data.dto.UserRequestDTO;
import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
	
	private final UserService userService;
	
	// 사용자 아이디(username)가 사용 중인지 확인
	@GetMapping("/check-username")
	public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
		boolean isAvailable = userService.isUsernameAvailable(username);
		return ResponseEntity.ok(isAvailable);
	}
	
	// 회원가입
	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO requestDTO) {
		UserResponseDTO responseDTO = userService.register(requestDTO);
		return ResponseEntity.ok(responseDTO);
	}
	
	// 로그인
	@PostMapping("/login")
	public ResponseEntity<UserResponseDTO> login(@RequestParam String username, @RequestParam String password) {
		UserResponseDTO responseDTO = userService.login(username, password);
		return ResponseEntity.ok(responseDTO);
	}
}