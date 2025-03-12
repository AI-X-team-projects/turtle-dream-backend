package com.example.turtledreambackend.data.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
	
	private String username;
	private String password;
	private String name;
	private String gender;
	private int age;
	private double height;
}