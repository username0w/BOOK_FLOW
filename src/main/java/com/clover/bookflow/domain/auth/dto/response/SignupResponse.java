package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.user.entity.User;

public record SignupResponse(

    String email,
    String nickname,
    String token

) {

  public static SignupResponse from(User user, String token) {
    return new SignupResponse(user.getEmail(), user.getNickname(), token);
  }

}
