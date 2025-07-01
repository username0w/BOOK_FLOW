package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;
import com.clover.bookflow.domain.member.entity.Member;

public record LoginResponse(

    String email,
    String nickname,
    AccessTokenInfo accessToken,
    RefreshTokenInfo refreshToken

) {

  public static LoginResponse from(Member member, TokenPair tokenPair) {
    return new LoginResponse(member.getEmail(), member.getNickname(),
        AccessTokenInfo.from(tokenPair.accessToken()),
        RefreshTokenInfo.from(tokenPair.refreshToken()));
  }

}
