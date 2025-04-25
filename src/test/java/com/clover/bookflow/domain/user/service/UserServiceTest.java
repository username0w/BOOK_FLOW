package com.clover.bookflow.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private UserService userService;

  // 필드 주입에서 생성자 주입으로 변경으로 인한 수정
  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder);
  }

  @Nested
  @DisplayName("회원 가입")
  class signup {

    @DisplayName("회원 가입 성공 테스트")
    @Test
    void signup_success() {
      // given
      UserSignupRequest request = new UserSignupRequest("hkd111@example.com",
          "password123", "길똥이");
      String encodedPW = "encodedPW";

      given(userRepository.existsByEmail("hkd111@example.com")).willReturn(false);
      given(passwordEncoder.encode(request.password())).willReturn(encodedPW);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

      User expectedUser = new User("hkd111@example.com", encodedPW, "길똥이");
      // id 없이 테스트 가능
      given(userRepository.save(any(User.class))).willReturn(expectedUser);

      // when
      UserSignupResponse response = userService.signup(request);

      // then
      verify(userRepository).existsByEmail(request.email());
      verify(passwordEncoder).encode(request.password());
      verify(userRepository).save(userCaptor.capture());

      User capturedUser = userCaptor.getValue();
      assertThat(capturedUser.getEmail()).isEqualTo(expectedUser.getEmail());
      assertThat(capturedUser.getPassword()).isEqualTo(encodedPW);
      assertThat(capturedUser.getNickname()).isEqualTo(expectedUser.getNickname());

      assertThat(response.nickname()).isEqualTo("길똥이");
      assertThat(response.email()).isEqualTo("hkd111@example.com");
    }

    // 이메일 중복시 회원 가입 실패
    @DisplayName("이미 가입된 이메일로 회원 가입하면 예외가 발생한다.")
    @Test
    void should_ThrowException_When_EmailAlreadyExists() {
      // given
      UserSignupRequest userSignupRequest = new UserSignupRequest("hkd111@example.com",
          "password123", "길똥이");
      given(userRepository.existsByEmail("hkd111@example.com")).willReturn(true);

      // when
      IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
          () -> userService.signup(userSignupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
    }

    // 닉네임 중복 시 회원 가입 실패
    @DisplayName("이미 존재하는 닉네임으로 회원 가입 시 예외가 발생한다.")
    @Test
    void should_ThrowException_When_NicknameAlreadyExists() {
      // given
      UserSignupRequest userSignupRequest = new UserSignupRequest("hkd111@example.com",
          "password123", "길똥이");
      given(userRepository.existsByNickname("길똥이")).willReturn(true);

      // when
      IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
          () -> userService.signup(userSignupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 존재하는 닉네임입니다.");
    }
  }


}
