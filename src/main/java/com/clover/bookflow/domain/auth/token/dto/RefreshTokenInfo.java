package com.clover.bookflow.domain.auth.token.dto;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;

public record RefreshTokenInfo(
    String token
) {

  public static RefreshTokenInfo from(TokenWithMeta tokenWithMeta) {
    return new RefreshTokenInfo(tokenWithMeta.token());
  }
}
