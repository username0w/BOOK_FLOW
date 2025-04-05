package com.clover.bookflow.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("회원 가입 성공 테스트")
  void signup() {
    // given
    UserSignupRequest request = new UserSignupRequest("hkd111@example.com",
        "password123", "길똥이");
    User expectedUser = new User("hkd111@example.com", "password123", "길똥이");
    // id 없이 테스트 가능
    given(userRepository.save(any(User.class))).willReturn(expectedUser);

    // when
    UserSignupResponse response = userService.signup(request);

    // then
    assertThat(response.nickname()).isEqualTo("길똥이");
    assertThat(response.email()).isEqualTo("hkd111@example.com");
  }

  // 이메일 중복시 회원 가입 실패
  @Test
  @DisplayName("이미 가입된 이메일로 회원 가입하면 예외가 발생한다.")
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


}
