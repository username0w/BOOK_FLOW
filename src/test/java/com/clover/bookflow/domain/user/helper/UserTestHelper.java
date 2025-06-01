package com.clover.bookflow.domain.user.helper;

import com.clover.bookflow.domain.auth.dto.AuthResponse;
import com.clover.bookflow.domain.auth.dto.LoginRequest;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;

public class UserTestHelper {

  private static final String DEFAULT_EMAIL = "test@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String DEFAULT_NICKNAME = "testNickname";

  public UserSignupRequest createSignupRequest() {
    return new UserSignupRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NICKNAME);
  }

  public UserSignupRequest createInvalidSignupRequest(String email, String password, String name) {
    return new UserSignupRequest(
        email != null ? email : DEFAULT_EMAIL,
        password != null ? password : DEFAULT_PASSWORD,
        name != null ? name : DEFAULT_NICKNAME
    );
  }

  public UserSignupResponse createSignupResponse() {
    return new UserSignupResponse(DEFAULT_EMAIL, DEFAULT_NICKNAME);
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

  public AuthResponse createAuthResponse() {
    return new AuthResponse(createSignupResponse(), "jwt-token");
  }

}
