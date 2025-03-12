package com.example.turtledreambackend.controller;

import com.example.turtledreambackend.data.dto.UserRequestDTO;
import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
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