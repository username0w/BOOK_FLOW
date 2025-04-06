package com.clover.bookflow.domain.user.service;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserSignupResponse signup(UserSignupRequest request) {
    // 이메일 중복 체크 (비즈니스 유효성 검증)
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    // User 도메인 객체 생성 (정적 팩토리 메서드 사용)
    User user = User.create(request.email(), encodedPassword, request.nickname());
    userRepository.save(user);

    return UserSignupResponse.from(user);
  }


}
