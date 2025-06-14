package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.user.entity.User;

public record LoginResponse(

    String email,
    String nickname,
    String token
) {

  public static LoginResponse from(User user, String token) {
    return new LoginResponse(user.getEmail(), user.getNickname(), token);
  }

}
