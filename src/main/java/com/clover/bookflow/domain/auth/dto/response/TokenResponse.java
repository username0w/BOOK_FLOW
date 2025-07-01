package com.clover.bookflow.domain.auth.dto.response;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.token.dto.AccessTokenInfo;
import com.clover.bookflow.domain.auth.token.dto.RefreshTokenInfo;

public record TokenResponse(
    AccessTokenInfo accessToken,
    RefreshTokenInfo refreshToken
) {

  public static TokenResponse from(TokenPair tokenPair) {
    return new TokenResponse(AccessTokenInfo.from(tokenPair.accessToken()),
        RefreshTokenInfo.from(tokenPair.refreshToken()));
  }

}
