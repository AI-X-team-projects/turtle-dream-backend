package com.example.turtledreambackend.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Document(collection = "user")
@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
	
	@Id
	private String id;             // MongoDB 내부 식별자 (자동 생성)
	
	@NotBlank(message = "사용자 아이디는 필수 값입니다.")
	private String username;       // 사용자 아이디 (고유 키로 활용 가능)
	
	@NotBlank(message = "비밀번호는 필수 값입니다.")
	private String password;       // 암호화된 비밀번호
	
	@NotBlank(message = "사용자 이름은 필수 값입니다.")
	private String name;           // 사용자 이름
	
	@NotBlank(message = "성별은 필수 값입니다.")
	private String gender;         // 성별 (예: 남/여)
	
	@PositiveOrZero(message = "나이는 음수가 될 수 없습니다.")
	private int age;               // 나이
	
	@PositiveOrZero(message = "키는 음수가 될 수 없습니다.")
	private double height;         // 키
}