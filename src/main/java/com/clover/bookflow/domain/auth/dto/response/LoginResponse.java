package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.member.entity.Member;

public record LoginResponse(

    MemberInfoResponse memberInfoResponse,
    TokenResponse tokenResponse

) {

  public static LoginResponse from(Member member, TokenPair tokenPair) {
    return new LoginResponse(
        MemberInfoResponse.from(member),
        TokenResponse.from(tokenPair));
  }

}
