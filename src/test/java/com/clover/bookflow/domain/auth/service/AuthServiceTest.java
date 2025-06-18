package com.clover.bookflow.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.helper.MemberTestHelper;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock
  private MemberService memberService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private MemberRepository memberRepository;

  private AuthService authService;

  private MemberTestHelper memberTestHelper;

  @BeforeEach
  void setUp() {
    authService = new AuthService(memberService, authenticationManager, jwtProvider,
        memberRepository);
    memberTestHelper = new MemberTestHelper();
  }

  @DisplayName("회원가입 후 JWT 토큰 발급 성공 테스트")
  @Test
  void shouldReturnToken_whenSignupCredentialsAreValid() {
    // given
    SignupRequest signupRequest = memberTestHelper.createSignupRequest();

    // Mock memberService의 회원가입 로직
    Member savedMember = new Member(signupRequest.email(), signupRequest.password(),
        signupRequest.nickname());
    given(memberService.signup(any(SignupRequest.class))).willReturn(savedMember);

    // Mock JWT 발급
    String expectedToken = "jwt-token";
    given(jwtProvider.createToken(any(Authentication.class))).willReturn(expectedToken);

    // when
    SignupResponse response = authService.signup(signupRequest);

    // then
    // 응답 검증
    assertThat(response.email()).isEqualTo(savedMember.getEmail());
    assertThat(response.nickname()).isEqualTo(savedMember.getNickname());
    assertThat(response.token()).isEqualTo(expectedToken);

    verify(memberService).signup(signupRequest);
    verify(jwtProvider).createToken(any(Authentication.class));
  }

  @DisplayName("MemberService.signup() 예외가 발생하면 AuthService.signup()도 예외를 던진다")
  @Test
  void shouldThrowException_whenUserServiceSignupThrows() {
    // given
    String existingEmail = "duplicate@example.com";
    SignupRequest signupRequest = memberTestHelper.createInvalidSignupRequest(existingEmail, null,
        null);
    given(memberService.signup(any(SignupRequest.class))).willThrow(
        new DuplicateResourceException(MemberErrorCode.EMAIL_ALREADY_EXISTS));

    // when, then
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
        () -> authService.signup(signupRequest));
    assertThat(exception.getMessage()).isEqualTo("이미 등록된 이메일입니다.");
  }

  @DisplayName("로그인 성공 테스트")
  @Test
  void shouldReturnToken_whenLoginCredentialsAreValid() {
    // given
    LoginRequest loginRequest = memberTestHelper.createLoginRequest();

    // 인증된 유저객체
    // memberRepository 에서 받아오기
    // UserDetails
    Member member = new Member("test@example.com", "password123", "개똥이");
    CustomMemberDetails userDetails = new CustomMemberDetails(member);

    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    // authenticationManager.authenticate 호출 시 위 Authentication 객체 리턴하도록 설정
    given(authenticationManager.authenticate(
        argThat(token ->
            token instanceof UsernamePasswordAuthenticationToken &&
                ((UsernamePasswordAuthenticationToken) token).getPrincipal()
                    .equals(loginRequest.email()) &&
                ((UsernamePasswordAuthenticationToken) token).getCredentials()
                    .equals(loginRequest.password())
        )
    )).willReturn(auth);
//    Authentication auth = new UsernamePasswordAuthenticationToken(user,
//        null); // 테스트 코드에서는 인증된 상태를 나타내는 것이므로 credentials null
//    given(authenticationManager.authenticate(
//        any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);

    given(memberRepository.findById(userDetails.getId()))
        .willReturn(Optional.of(member));

    String expectedToken = "jwt-token";
    given(jwtProvider.createToken(auth)).willReturn(expectedToken);

    // when
    LoginResponse response = authService.login(loginRequest);

    // then
    assertThat(response.email()).isEqualTo(loginRequest.email());
    assertThat(response.token()).isEqualTo(expectedToken);

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtProvider).createToken(auth);

  }

  @DisplayName("틀린 비밀번호로 로그인하면 401 Unauthorized 예외가 발생한다")
  @Test
  void shouldThrowUnauthorizedException_whenPasswordIsInvalid() {
    // given
    String wrongPassword = "wrongPassword";
    LoginRequest loginRequest = memberTestHelper.createInvalidLoginRequest(null, wrongPassword);

    // authenticationManager.authenticate(...) 호출 시 비밀번호 오류로 인증 실패 시뮬레이션
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // when
    UnauthorizedException exception = assertThrows(
        UnauthorizedException.class,
        () -> authService.login(loginRequest));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.LOGIN_FAILED);

    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));

  }

  @DisplayName("존재하지 않는 이메일로 로그인하면 401 Unauthorized 예외가 발생한다")
  @Test
  void shouldThrowUnauthorizedException_whenEmailIsInvalid() {
    // given
    String wrongEmail = "wrong@example.com";
    LoginRequest loginRequest = memberTestHelper.createInvalidLoginRequest(wrongEmail, null);

    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // when
    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> authService.login(loginRequest));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.LOGIN_FAILED);

    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));

  }


}
