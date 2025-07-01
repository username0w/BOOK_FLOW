package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;
import com.clover.bookflow.domain.member.entity.Member;

public record SignupResponse(

    String email,
    String nickname,
    AccessTokenInfo accessToken,
    RefreshTokenInfo refreshToken

) {

  public static SignupResponse from(Member member, TokenPair tokenPair) {
    return new SignupResponse(member.getEmail(), member.getNickname(),
        AccessTokenInfo.from(tokenPair.accessToken()),
        RefreshTokenInfo.from(tokenPair.refreshToken()));
  }

}
