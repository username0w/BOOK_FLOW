package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.member.entity.Member;

public record LoginResponse(

    String email,
    String nickname,
    String token
) {

  public static LoginResponse from(Member member, String token) {
    return new LoginResponse(member.getEmail(), member.getNickname(), token);
  }

}
