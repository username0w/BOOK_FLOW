package com.clover.bookflow.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 단순 데이터 전달 용도의 DTO 이므로 record 사용 (불변성 보장)
public record UserSignupRequest(
    // 입력값 검증은 Bean Validation (@Valid)를 사용하며, Controller 진입 직전에 처리
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    String password,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    String nickname
) {

  // DTO 에 toEntity 메서드를 두지 않음: Entity 생성 책임은 도메인 내부(User.create)에서 수행
}
