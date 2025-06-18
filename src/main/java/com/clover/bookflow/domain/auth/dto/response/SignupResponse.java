package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.member.entity.Member;

public record SignupResponse(

    String email,
    String nickname,
    String token

) {

  public static SignupResponse from(Member member, String token) {
    return new SignupResponse(member.getEmail(), member.getNickname(), token);
  }

}
