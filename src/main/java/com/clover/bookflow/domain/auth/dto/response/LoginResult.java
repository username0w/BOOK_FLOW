package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.member.entity.Member;

public record LoginResult(
    MemberInfoResponse memberInfoResponse,
    TokenResponse tokenResponse

) {

  public static LoginResult from(Member member, TokenPair tokenPair) {
    return new LoginResult(
        MemberInfoResponse.from(member),
        TokenResponse.from(tokenPair)
    );
  }
}
