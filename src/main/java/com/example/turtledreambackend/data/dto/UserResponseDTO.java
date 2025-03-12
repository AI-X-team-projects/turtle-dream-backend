package com.example.turtledreambackend.data.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
	
	private String id;
	private String username;
	private String name;
	private String gender;
	private int age;
	private double height;
}