package com.example.turtledreambackend.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-12
 *
 * 수정자 : 김동규
 * 수정일 : 2025-03-13
 * 수정이유 : 사용자 테이블 추가
 * createdAt, lastLogin, alertPreference, postureDataRef
 *
 * 사용자 엔티티
 * */
@Document(collection = "user")  // MongoDB 컬렉션 명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	private String id;  // MongoDB 자동 생성 ObjectId

	@NotBlank(message = "사용자 아이디는 필수 값입니다.")
	private String username;  // 사용자 아이디 (고유)

	@NotBlank(message = "비밀번호는 필수 값입니다.")
	private String password;  // 암호화된 비밀번호

	@NotBlank(message = "사용자 이름은 필수 값입니다.")
	private String name;  // 사용자 이름

	@NotBlank(message = "성별은 필수 값입니다.")
	private String gender;  // 성별 (예: 남/여)

	@PositiveOrZero(message = "나이는 음수가 될 수 없습니다.")
	private int age;  // 나이

	@PositiveOrZero(message = "키는 음수가 될 수 없습니다.")
	private double height;  // 키 (단위: cm)

	@CreatedDate
	private Instant createdAt;  // 가입 날짜

	@LastModifiedDate
	private Instant lastLogin;  // 마지막 로그인 시간

	private String postureDataRef;  // 거북목 감지 데이터와 연결 (posture_logs 컬렉션 ID)

}