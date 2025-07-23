package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;

public record TokenResult(
    AccessTokenInfo accessToken,
    RefreshTokenInfo refreshToken
) {

  public static TokenResult from(TokenPair tokenPair) {
    return new TokenResult(AccessTokenInfo.from(tokenPair.accessToken()),
        RefreshTokenInfo.from(tokenPair.refreshToken()));
  }

}
