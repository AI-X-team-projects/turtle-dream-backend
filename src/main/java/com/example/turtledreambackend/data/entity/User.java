package com.example.turtledreambackend.data.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 최초 작성자 : 류재영
 * 최초 작성일 : 2025-03-12
 *
 * 수정자 : 김동규
 * 수정일 : 2025-03-13
 * 수정이유 : 사용자 테이블 추가
 * createdAt, lastLogin, alertPreference, postureDataRef
 *
 * 수정자 : 류재영
 * 수정일 : 2025-03-14
 * 수정이유 : OAuth2 로그인 지원을 위한 필드 추가
 * provider, providerId, email, imageUrl
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

	private String password;  // 암호화된 비밀번호 (소셜 로그인 사용자는 null일 수 있음)

	@NotBlank(message = "사용자 이름은 필수 값입니다.")
	private String name;  // 사용자 이름

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
	
	// OAuth2 관련 필드
	private String provider;      // 소셜 로그인 제공자 (google, naver 등)
	private String providerId;    // 소셜 로그인 제공자의 ID
	private String email;         // 이메일
	private String imageUrl;      // 프로필 이미지 URL
	private boolean emailVerified; // 이메일 인증 여부
	
	/**
	 * OAuth2 사용자 정보로 User 객체를 생성하는 정적 팩토리 메서드
	 * 
	 * @param oAuth2UserInfo OAuth2 사용자 정보
	 * @return User 객체
	 */
	public static User createFromOAuth2UserInfo(com.example.turtledreambackend.config.oauth2.OAuth2UserInfo oAuth2UserInfo) {
		return User.builder()
				.username(oAuth2UserInfo.getEmail()) // 이메일을 사용자 아이디로 사용
				.name(oAuth2UserInfo.getName())
				.email(oAuth2UserInfo.getEmail())
				.provider(oAuth2UserInfo.getProvider())
				.providerId(oAuth2UserInfo.getId())
				.imageUrl(oAuth2UserInfo.getImageUrl())
				.emailVerified(true) // OAuth2 로그인은 이메일이 인증된 것으로 간주
				.createdAt(Instant.now())
				.lastLogin(Instant.now())
				.build();
	}
	
	/**
	 * OAuth2 사용자 정보로 User 객체를 업데이트하는 메서드
	 * 
	 * @param oAuth2UserInfo OAuth2 사용자 정보
	 */
	public void updateFromOAuth2UserInfo(com.example.turtledreambackend.config.oauth2.OAuth2UserInfo oAuth2UserInfo) {
		this.name = oAuth2UserInfo.getName();
		this.imageUrl = oAuth2UserInfo.getImageUrl();
		this.lastLogin = Instant.now();
	}
}