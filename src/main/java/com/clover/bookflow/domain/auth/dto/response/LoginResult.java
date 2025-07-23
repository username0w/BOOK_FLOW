package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.member.entity.Member;

public record LoginResult(
    MemberInfoResponse memberInfoResponse,
    TokenResult tokenResult

) {

  public static LoginResult from(Member member, TokenPair tokenPair) {
    return new LoginResult(
        MemberInfoResponse.from(member),
        TokenResult.from(tokenPair)
    );
  }
}
