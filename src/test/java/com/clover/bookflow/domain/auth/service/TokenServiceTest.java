package com.clover.bookflow.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.dto.response.TokenResult;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.auth.token.service.TokenService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.auth.InvalidTokenException;
import com.clover.bookflow.global.exception.auth.TokenExpiredException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  private TokenService tokenService;

  private AuthTestHelper authTestHelper;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService(jwtProvider, memberRepository, refreshTokenRepository);
    authTestHelper = new AuthTestHelper();
  }

  @DisplayName("UUID 와 역할을 넘기면 TokenPair 가 발급된다")
  @Test
  void shouldReturnTokenPair_whenIssueTokens() {
    // given
    Member member = MemberTestHelper.createTestUser();
    List<String> roles = List.of("ROLE_" + member.getRole().name());

    given(jwtProvider.createAccessToken(any(), any())).willReturn(
        AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA);
    given(jwtProvider.createRefreshToken(any())).willReturn(
        AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA);

    // when
    TokenPair result = tokenService.issueTokens(member.getUuid(), roles);

    // then
    assertThat(result.accessToken()).isEqualTo(AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA);
    assertThat(result.refreshToken()).isEqualTo(AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA);
  }

  @DisplayName("유효한 RefreshToken으로 토큰 재발급 시 새로운 Access/RefreshToken이 발급된다")
  @Test
  void shouldReturnNewTokenPair_whenValidRefreshToken() {
    Member member = MemberTestHelper.createTestUser();
    RefreshToken savedToken = AuthTestHelper.createRefreshToken(member);
    String jti = AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA.jti();
    String refreshToken = AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA.token();

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.findByJti(jti)).willReturn(Optional.of(savedToken));
    given(jwtProvider.getUuidFromToken(refreshToken)).willReturn(member.getUuid());
    given(memberRepository.findByUuid(member.getUuid())).willReturn(Optional.of(member));
    given(jwtProvider.createAccessToken(any(), any())).willReturn(
        AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA);
    given(jwtProvider.createRefreshToken(any())).willReturn(
        AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA);

    // when
    TokenResult result = tokenService.refreshToken(refreshToken);

    // then
    assertThat(result.accessToken().token()).isEqualTo(
        AuthTestHelper.DEFAULT_ACCESS_TOKENWITHMETA.token());
    assertThat(result.accessToken().expiresAt()).isEqualTo(
        AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA.expiresAt());
    assertThat(result.refreshToken().token()).isEqualTo(
        AuthTestHelper.DEFAULT_REFRESH_TOKENWITHMETA.token());
    verify(refreshTokenRepository).save(any());
  }

  @DisplayName("DB 에 없는 토큰으로 재발급 요청하면 InvalidTokenException 예외가 발생한다")
  @Test
  void shouldThrowInvalidTokenException_whenTokenNotFound() {
    String refreshToken = "invalidRefreshToken";
    String jti = "jti-not-found";

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.findByJti(jti)).willReturn(Optional.empty());

    assertThrows(InvalidTokenException.class, () -> tokenService.refreshToken(refreshToken));
  }

  @DisplayName("만료된 RefreshToken으로 재발급 요청하면 TokenExpiredException 예외가 발생한다")
  @Test
  void shouldThrowTokenExpiredException_whenTokenExpired() {
    Member member = MemberTestHelper.createTestUser();
    String refreshToken = "expired-token";
    String jti = "expired-jti";
    RefreshToken expiredToken = AuthTestHelper.createExpiredRefreshToken(member);

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.findByJti(jti)).willReturn(Optional.of(expiredToken));

    assertThrows(TokenExpiredException.class, () -> tokenService.refreshToken(refreshToken));
  }

  @DisplayName("Active 상태 아닌 멤버로 재발급 요청하면 BusinessException 예외가 발생한다")
  @Test
  void shouldThrowBusinessException_whenMemberNotActive() {
    String refreshToken = "token";
    String jti = "jti";
    Member inactiveMember = MemberTestHelper.createTestUser();
    inactiveMember.withdraw();
    RefreshToken validToken = new RefreshToken(inactiveMember, refreshToken,
        Instant.now().plusSeconds(3600), jti);

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.findByJti(jti)).willReturn(Optional.of(validToken));
    given(jwtProvider.getUuidFromToken(refreshToken)).willReturn(inactiveMember.getUuid());
    given(memberRepository.findByUuid(inactiveMember.getUuid()))
        .willReturn(Optional.of(inactiveMember));

    assertThrows(BusinessException.class, () -> tokenService.refreshToken(refreshToken));
  }

  @DisplayName("DB에 없는 멤버로 재발급 요청하면 BusinessException 예외가 발생한다")
  @Test
  void shouldThrowBusinessException_whenMemberNotFound() {
    String refreshToken = "token";
    String jti = "jti";
    Member member = MemberTestHelper.createTestUser();
    RefreshToken validToken = new RefreshToken(member, refreshToken,
        Instant.now().plusSeconds(3600), jti);

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.findByJti(jti)).willReturn(Optional.of(validToken));
    given(jwtProvider.getUuidFromToken(refreshToken)).willReturn(member.getUuid());
    given(memberRepository.findByUuid(member.getUuid())).willReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> tokenService.refreshToken(refreshToken));
  }

  @DisplayName("Refresh Token의 JTI로 토큰을 삭제하여 로그아웃 처리한다")
  @Test
  void shouldDeleteToken_whenValidToken() {
    String refreshToken = "refresh";
    String jti = "logout-jti";

    given(jwtProvider.getJtiFromToken(refreshToken)).willReturn(jti);
    given(refreshTokenRepository.existsByJti(jti)).willReturn(true);

    tokenService.logout(refreshToken);

    verify(refreshTokenRepository).deleteByJti(jti);
  }


}
