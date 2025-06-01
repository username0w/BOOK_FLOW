package com.clover.bookflow.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.dto.AuthResponse;
import com.clover.bookflow.domain.auth.dto.LoginRequest;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.helper.UserTestHelper;
import com.clover.bookflow.domain.user.service.UserService;
import com.clover.bookflow.global.errorcode.UserErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtProvider jwtProvider;

  private AuthService authService;

  private UserTestHelper userTestHelper;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userService, authenticationManager, jwtProvider);
    userTestHelper = new UserTestHelper();
  }

  @DisplayName("회원가입 후 JWT 토큰 발급 성공 테스트")
  @Test
  void signup_success() {
    // given
    UserSignupRequest signupRequest = userTestHelper.createSignupRequest();

    // Mock userService의 회원가입 로직
    UserSignupResponse savedUser = new UserSignupResponse(signupRequest.email(),
        signupRequest.nickname());
    given(userService.signup(any(UserSignupRequest.class))).willReturn(savedUser);

    // Mock JWT 발급
    String expectedToken = "jwt-token";
    given(jwtProvider.createToken(any(Authentication.class))).willReturn(expectedToken);

    // when
    AuthResponse response = authService.signup(signupRequest);

    // then
    // 응답 검증
    assertThat(response.userSignupResponse().email()).isEqualTo(savedUser.email());
    assertThat(response.userSignupResponse().nickname()).isEqualTo(savedUser.nickname());
    assertThat(response.token()).isEqualTo(expectedToken);

    verify(userService).signup(signupRequest);
    verify(jwtProvider).createToken(any(Authentication.class));
  }

  @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생")
  @Test
  void shouldThrowException_whenEmailAlreadyExists() {
    // given
    String existingEmail = "duplicate@example.com";
    UserSignupRequest signupRequest = userTestHelper.createInvalidSignupRequest(existingEmail, null,
        null);
    given(userService.signup(any(UserSignupRequest.class))).willThrow(
        new DuplicateResourceException(UserErrorCode.EMAIL_ALREADY_EXISTS));

    // when, then
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
        () -> authService.signup(signupRequest));
    assertThat(exception.getMessage()).isEqualTo("이미 등록된 이메일입니다.");
  }

  @DisplayName("로그인 성공 테스트")
  @Test
  void login_success() {
    // given
    LoginRequest loginRequest = userTestHelper.createLoginRequest();

    // 인증된 유저객체
    // userRepository 에서 받아오기
    // UserDetails
    User user = new User("test@example.com", "password123", "개똥이");

    Authentication auth = new UsernamePasswordAuthenticationToken(user,
        null); // 테스트 코드에서는 인증된 상태를 나타내는 것이므로 credentials null
    given(authenticationManager.authenticate(
        any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);

    String expectedToken = "jwt-token";
    given(jwtProvider.createToken(auth)).willReturn(expectedToken);

    // when
    AuthResponse response = authService.login(loginRequest);

    // then
    assertThat(response.userSignupResponse().email()).isEqualTo(loginRequest.email());
    assertThat(response.token()).isEqualTo(expectedToken);

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtProvider).createToken(auth);

  }

  @DisplayName("로그인 실패 테스트 - 비밀번호 오류")
  @Test
  void should_ThrowException_When_wrong_password() {
    // given
    String wrongPassword = "wrongPassword";
    LoginRequest loginRequest = userTestHelper.createInvalidLoginRequest(null, wrongPassword);

    // authenticationManager.authenticate(...) 호출 시 비밀번호 오류로 인증 실패 시뮬레이션
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // when
    UnauthorizedException exception = assertThrows(
        UnauthorizedException.class,
        () -> authService.login(loginRequest));

    // then
    assertThat(exception.getMessage()).contains("비밀번호가 일치하지 않습니다.");

    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));

  }

  @DisplayName("로그인 실패 테스트 - 이메일 오류")
  @Test
  void should_ThrowException_When_invalid_email() {
    // given
    String wrongEmail = "wrong@example.com";
    LoginRequest loginRequest = userTestHelper.createInvalidLoginRequest(wrongEmail, null);

    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new UsernameNotFoundException("Username not found"));

    // when
    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> authService.login(loginRequest));

    // then
    assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다.");

    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));

  }


}
