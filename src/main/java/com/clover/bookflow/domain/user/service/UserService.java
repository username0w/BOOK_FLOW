package com.clover.bookflow.domain.user.service;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
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

    // 이메일 중복 체크 (비즈니스 유효성 검증)
    validateEmail(request.email());

    String encodedPassword = passwordEncoder.encode(request.password());

    // User 도메인 객체 생성 (정적 팩토리 메서드 사용)
    User user = User.create(request.email(), encodedPassword, request.nickname());
    userRepository.save(user);

    return UserSignupResponse.from(user);
  }

  private void validateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
    }
  }


}
