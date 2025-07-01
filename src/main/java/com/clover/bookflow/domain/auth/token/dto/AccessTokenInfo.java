package com.clover.bookflow.domain.auth.token.dto;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import java.time.Instant;

public record AccessTokenInfo(
    String token,
    Instant expiresAt
) {

  public static AccessTokenInfo from(TokenWithMeta tokenWithMeta) {
    return new AccessTokenInfo(tokenWithMeta.token(), tokenWithMeta.expiresAt());
  }
}
