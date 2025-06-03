package com.clover.bookflow.domain.auth.dto;

import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;

public record AuthResponse(
    UserSignupResponse userSignupResponse,
    String token
) {

  public static AuthResponse from(UserSignupResponse response, String token) {
    return new AuthResponse(response, token);
  }

}
