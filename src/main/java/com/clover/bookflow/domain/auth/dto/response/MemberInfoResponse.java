package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.member.entity.Member;

public record MemberInfoResponse(
    String email,
    String nickname
) {

  public static MemberInfoResponse from(Member member) {
    return new MemberInfoResponse(member.getEmail(), member.getNickname());
  }

}
