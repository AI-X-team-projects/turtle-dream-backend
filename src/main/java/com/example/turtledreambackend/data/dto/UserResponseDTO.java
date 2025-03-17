package com.example.turtledreambackend.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
	private String id;
	private String username;
	private String name;
	private String gender;
	private int age;
	private double height;
	
	// OAuth2 인증에 필요한 필드
	private boolean authenticated;
	private String message;
	private String userId;
	private String email;
}
