package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.member.entity.Member;

public record SignupResult(
    MemberInfoResponse memberInfoResponse,
    TokenResult tokenResult
) {

  public static SignupResult from(Member member, TokenPair tokenPair) {
    return new SignupResult(
        MemberInfoResponse.from(member),
        TokenResult.from(tokenPair)
    );
  }
}
