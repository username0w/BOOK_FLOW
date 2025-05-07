package com.clover.bookflow.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 단순 데이터 전달 용도의 DTO 이므로 record 사용 (불변성 보장)
public record UserSignupRequest(
    // 입력값 검증은 Bean Validation (@Valid)를 사용하며, Controller 진입 직전에 처리
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    String password,

    @NotBlank(message = "Nickname must not be blank")
    @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
    String nickname
) {

  // DTO 에 toEntity 메서드를 두지 않음: Entity 생성 책임은 도메인 내부(User.create)에서 수행
}
