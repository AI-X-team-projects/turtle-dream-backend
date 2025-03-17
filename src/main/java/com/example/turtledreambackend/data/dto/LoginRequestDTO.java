package com.example.turtledreambackend.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {
    
    @NotBlank(message = "사용자 아이디는 필수 값입니다.")
    private String username;
    
    @NotBlank(message = "비밀번호는 필수 값입니다.")
    private String password;
} 