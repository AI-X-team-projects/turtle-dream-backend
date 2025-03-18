package com.example.turtledreambackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.turtledreambackend.data.dto.LoginRequestDTO;
import com.example.turtledreambackend.data.dto.UserRequestDTO;
import com.example.turtledreambackend.data.dto.UserResponseDTO;
import com.example.turtledreambackend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 *
 * 작성자 : 류재영
 * 작성일 : 2025-03-12
 *
 * 사용자 관련 컨트롤러
 *
 **/
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
	
	/**
	 * 회원가입 API
	 * 
	 * @param userRequestDTO 회원가입 요청 DTO
	 * @return 회원가입 결과
	 */
	@PostMapping("/signup")
	public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserRequestDTO userRequestDTO) {
		UserResponseDTO userResponseDTO = userService.createUser(userRequestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
	}
	
	/**
	 * 로그인 API
	 * 
	 * @param loginRequestDTO 로그인 요청 DTO
	 * @return 로그인 결과
	 */
	@PostMapping("/login")
	public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
		UserResponseDTO userResponseDTO = userService.login(loginRequestDTO);
		
		// 로그인 성공 시 세션에 사용자 정보 저장
		if (userResponseDTO != null) {
			HttpSession session = request.getSession();
			session.setAttribute("userId", userResponseDTO.getId());
			session.setAttribute("username", userResponseDTO.getUsername());
			session.setAttribute("name", userResponseDTO.getName());
		}
		
		return ResponseEntity.ok(userResponseDTO);
	}
	
	/**
	 * 로그아웃 API
	 * 
	 * @param request HTTP 요청
	 * @return 로그아웃 결과
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.ok().build();
	}
}