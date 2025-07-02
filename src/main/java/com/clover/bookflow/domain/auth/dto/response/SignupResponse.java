package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.member.entity.Member;

public record SignupResponse(

    MemberInfoResponse memberInfoResponse,
    TokenResponse tokenResponse

) {

  public static SignupResponse from(Member member, TokenPair tokenPair) {
    return new SignupResponse(
        MemberInfoResponse.from(member),
        TokenResponse.from(tokenPair));
  }

}
