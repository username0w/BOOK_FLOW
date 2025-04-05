package com.clover.bookflow.domain.user.dto.response;

import com.clover.bookflow.domain.user.entity.User;

public record UserSignupResponse(

    Long id,
    String email,
    String nickname

) {

  public static UserSignupResponse from(User user) {
    return new UserSignupResponse(user.getId(), user.getEmail(), user.getNickname());
  }

}
