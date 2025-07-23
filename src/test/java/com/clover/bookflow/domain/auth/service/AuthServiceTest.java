package com.clover.bookflow.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResult;
import com.clover.bookflow.domain.auth.dto.response.SignupResult;
import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.auth.token.service.TokenService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import java.util.Optional;
import java.util.UUID;
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
  private TokenService tokenService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  private AuthService authService;

  private AuthTestHelper authTestHelper;

  @BeforeEach
  void setUp() {
    authService = new AuthService(memberService, tokenService, authenticationManager,
        memberRepository, refreshTokenRepository);
    authTestHelper = new AuthTestHelper();
  }

  @DisplayName("회원가입 성공 시 AccessToken 과 RefreshToken 이 발급된다")
  @Test
  void shouldReturnToken_whenSignupCredentialsAreValid() {
    // given
    SignupRequest signupRequest = authTestHelper.createSignupRequest();

    // Mock memberService의 회원가입 로직
    Member savedMember = MemberTestHelper.createTestUser();
    given(memberService.signup(any(SignupRequest.class))).willReturn(savedMember);

    // Mock JWT 발급
    given(tokenService.issueTokens(any(UUID.class), anyList())).willReturn(
        TokenPair.of(
            AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA,
            AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA
        )
    );

    // when
    SignupResult result = authService.signup(signupRequest);

    // then
    assertThat(result.memberInfoResponse().email()).isEqualTo(savedMember.getEmail());
    assertThat(result.memberInfoResponse().nickname()).isEqualTo(savedMember.getNickname());
    assertThat(result.tokenResult().accessToken()).isEqualTo(
        AccessTokenInfo.from(AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA));
    assertThat(result.tokenResult().refreshToken()).isEqualTo(
        RefreshTokenInfo.from(AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA));

    verify(memberService).signup(signupRequest);
    verify(refreshTokenRepository).save(any(RefreshToken.class));

  }

  @DisplayName("MemberService.signup() 예외가 발생하면 AuthService.signup()도 예외를 던진다")
  @Test
  void shouldThrowException_whenUserServiceSignupThrows() {
    // given
    String existingEmail = "duplicate@example.com";
    SignupRequest signupRequest = authTestHelper.createInvalidSignupRequest(existingEmail, null,
        null);
    given(memberService.signup(any(SignupRequest.class))).willThrow(
        new DuplicateResourceException(MemberErrorCode.EMAIL_ALREADY_EXISTS));

    // when, then
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
        () -> authService.signup(signupRequest));
    assertThat(exception.getMessage()).isEqualTo("이미 등록된 이메일입니다.");
  }

  @DisplayName("로그인 성공 시 AccessToken 과 RefreshToken 이 발급된다")
  @Test
  void shouldReturnTokensAndStoreRefreshToken_whenLoginCredentialsAreValid() {
    // given
    LoginRequest loginRequest = authTestHelper.createLoginRequest();

    // 인증된 유저객체
    // memberRepository 에서 받아오기
    // UserDetails
    // UUID 필요한 것을 위해 별도 존재
    Member member = MemberTestHelper.createTestUser();
    CustomMemberDetails userDetails = new CustomMemberDetails(member);

    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
    // 테스트 코드에서는 인증된 상태를 나타내는 것이므로 credentials null

    // authenticationManager.authenticate 호출 시 위 Authentication 객체 리턴하도록 설정
    given(authenticationManager.authenticate(
        any())).willReturn(auth);

    given(memberRepository.findById(userDetails.getId()))
        .willReturn(Optional.of(member));

    // 토큰 발급
    given(tokenService.issueTokens(any(UUID.class), anyList())).willReturn(
        TokenPair.of(
            AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA,
            AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA
        )
    );

    // when
    LoginResult result = authService.login(loginRequest);

    // then
    assertThat(result.memberInfoResponse().email()).isEqualTo(loginRequest.email());
    assertThat(result.tokenResult().accessToken()).isEqualTo(
        AccessTokenInfo.from(AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA));
    assertThat(result.tokenResult().refreshToken()).isEqualTo(
        RefreshTokenInfo.from(AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(refreshTokenRepository).save(any(RefreshToken.class));

  }

  @DisplayName("틀린 비밀번호로 로그인하면 401 Unauthorized 예외가 발생한다")
  @Test
  void shouldThrowUnauthorizedException_whenPasswordIsInvalid() {
    // given
    String wrongPassword = "wrongPassword";
    LoginRequest loginRequest = authTestHelper.createInvalidLoginRequest(null, wrongPassword);

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
    LoginRequest loginRequest = authTestHelper.createInvalidLoginRequest(wrongEmail, null);

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
