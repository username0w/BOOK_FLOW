package com.clover.bookflow.domain.auth;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.member.entity.Member;
import java.time.Instant;

public class AuthTestHelper {

  private static final String DEFAULT_EMAIL = "test@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String DEFAULT_NICKNAME = "testNickname";
  private static final String DEFAULT_ACCESS_TOKEN = "testAccessToken";
  private static final String DEFAULT_REFRESH_TOKEN = "testRefreshToken";
  private static final String DEFAULT_JTI_A = "testJtiA";
  private static final String DEFAULT_JTI_R = "testJtiR";
  private static final Instant DEFAULT_FIXED_EXPIRATION = Instant.parse("2099-01-01T00:00:00Z");

  public static final TokenWithMeta DEFAULT_ACCESS_TOKENWITHMETA = new TokenWithMeta(
      DEFAULT_ACCESS_TOKEN, DEFAULT_JTI_A, DEFAULT_FIXED_EXPIRATION);
  public static final TokenWithMeta DEFAULT_REFRESH_TOKENWITHMETA = new TokenWithMeta(
      DEFAULT_REFRESH_TOKEN, DEFAULT_JTI_R, DEFAULT_FIXED_EXPIRATION);

  private static final AccessTokenInfo DEFAULT_ACCESS_TOKEN_INFO = new AccessTokenInfo(
      DEFAULT_ACCESS_TOKEN, DEFAULT_FIXED_EXPIRATION);
  private static final RefreshTokenInfo DEFAULT_REFRESH_TOKEN_INFO = new RefreshTokenInfo(
      DEFAULT_REFRESH_TOKEN);

  public static Member testUserMember() {
    return new Member(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NICKNAME);
  }

  public SignupRequest createSignupRequest() {
    return new SignupRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NICKNAME);
  }

  public SignupRequest createInvalidSignupRequest(String email, String password, String name) {
    return new SignupRequest(
        email != null ? email : DEFAULT_EMAIL,
        password != null ? password : DEFAULT_PASSWORD,
        name != null ? name : DEFAULT_NICKNAME
    );
  }

  public SignupResponse createSignupResponse() {
    return new SignupResponse(DEFAULT_EMAIL, DEFAULT_NICKNAME, DEFAULT_ACCESS_TOKEN_INFO,
        DEFAULT_REFRESH_TOKEN_INFO);
  }

  public LoginRequest createLoginRequest() {
    return new LoginRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
  }

  public LoginRequest createInvalidLoginRequest(String email, String password) {
    return new LoginRequest(
        email != null ? email : DEFAULT_EMAIL,
        password != null ? password : DEFAULT_PASSWORD
    );
  }

  public LoginResponse createLoginResponse() {
    return new LoginResponse(DEFAULT_EMAIL, DEFAULT_NICKNAME, DEFAULT_ACCESS_TOKEN_INFO,
        DEFAULT_REFRESH_TOKEN_INFO);
  }

  public static RefreshToken createRefreshToken(Member member) {
    return RefreshToken.create(member, DEFAULT_REFRESH_TOKENWITHMETA);
  }

  public static RefreshToken createExpiredRefreshToken(Member member) {
    return new RefreshToken(member, "expired-token", Instant.now().minusSeconds(1), "expired-jti");
  }
}
