package com.clover.bookflow.domain.user.service;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import com.clover.bookflow.global.errorcode.UserErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor // 생성자 주입이 더 안전, 깔끔
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserSignupResponse signup(UserSignupRequest request) {
    log.info("회원가입 시도: {}", request);

    // 입력값 자체 검사는 커스텀 어노테이션
    // db 조회 필요한 검사는 서비스에서 처리

    // 이메일, 닉네임 중복 체크 (비즈니스 유효성 검증)
    validateDuplicateEmail(request.email());
    validateDuplicateNickname(request.nickname());

    String encodedPassword = passwordEncoder.encode(request.password());

    // User 도메인 객체 생성 (정적 팩토리 메서드 사용)
    User user = User.create(request.email(), encodedPassword, request.nickname());
    userRepository.save(user);

    return UserSignupResponse.from(user);
  }

  private void validateDuplicateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateResourceException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  private void validateDuplicateNickname(String nickname) {
    if (userRepository.existsByNickname(nickname)) {
      throw new DuplicateResourceException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
    }
  }


}
