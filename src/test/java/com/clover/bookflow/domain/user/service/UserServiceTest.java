package com.clover.bookflow.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.helper.UserTestHelper;
import com.clover.bookflow.domain.user.repository.UserRepository;
import com.clover.bookflow.global.exception.DuplicateResourceException;
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

  private UserTestHelper userTestHelper;

  // 필드 주입에서 생성자 주입으로 변경으로 인한 수정
  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder);
    userTestHelper = new UserTestHelper();
  }

  @Nested
  @DisplayName("회원 가입")
  class signup {

    @DisplayName("회원 가입 성공 테스트")
    @Test
    void shouldSaveUser_whenSignupCredentialsAreValid() {
      // given
      SignupRequest request = userTestHelper.createSignupRequest();
      String encodedPW = "encodedPW";

      given(userRepository.existsByEmail(request.email())).willReturn(false);
      given(passwordEncoder.encode(request.password())).willReturn(encodedPW);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

      User expectedUser = new User(request.email(), encodedPW, request.nickname());
      // id 없이 테스트 가능
      given(userRepository.save(any(User.class))).willReturn(expectedUser);

      // when
      User savedUser = userService.signup(request);

      // then
      // 결과 검증
      assertThat(savedUser.getNickname()).isEqualTo(request.nickname());
      assertThat(savedUser.getEmail()).isEqualTo(request.email());

      // 내부 호출 검증
      verify(userRepository).existsByEmail(request.email());
      verify(passwordEncoder).encode(request.password());
      verify(userRepository).save(userCaptor.capture());

      // 캡쳐된 인자 값 검증
      User capturedUser = userCaptor.getValue();
      assertThat(capturedUser.getEmail()).isEqualTo(expectedUser.getEmail());
      assertThat(capturedUser.getPassword()).isEqualTo(encodedPW);
      assertThat(capturedUser.getNickname()).isEqualTo(expectedUser.getNickname());
    }

    // 이메일 중복시 회원 가입 실패
    @DisplayName("이미 가입된 이메일로 회원 가입하면 예외가 발생한다")
    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
      // given
      String existingEmail = "hkd111@example.com";
      SignupRequest signupRequest = userTestHelper.createInvalidSignupRequest(existingEmail,
          null, null);
      given(userRepository.existsByEmail(existingEmail)).willReturn(true);

      // when
      DuplicateResourceException e = assertThrows(DuplicateResourceException.class,
          () -> userService.signup(signupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 등록된 이메일입니다.");
    }

    // 닉네임 중복 시 회원 가입 실패
    @DisplayName("이미 존재하는 닉네임으로 회원 가입 시 예외가 발생한다")
    @Test
    void shouldThrowException_WhenNicknameAlreadyExists() {
      // given
      String duplicatedNickname = "길똥이";
      SignupRequest signupRequest = userTestHelper.createInvalidSignupRequest(null, null,
          duplicatedNickname);
      given(userRepository.existsByNickname(duplicatedNickname)).willReturn(true);

      // when
      DuplicateResourceException e = assertThrows(DuplicateResourceException.class,
          () -> userService.signup(signupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 존재하는 닉네임입니다.");
    }
  }


}
