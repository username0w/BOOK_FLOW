package com.clover.bookflow.domain.member.helper;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;

public class MemberTestHelper {

  private static final String DEFAULT_EMAIL = "test@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String DEFAULT_NICKNAME = "testNickname";
  private static final String DEFAULT_TOKEN = "testToken";


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
    return new SignupResponse(DEFAULT_EMAIL, DEFAULT_NICKNAME, DEFAULT_TOKEN);
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
    return new LoginResponse(DEFAULT_EMAIL, DEFAULT_NICKNAME, DEFAULT_TOKEN);
  }

}
