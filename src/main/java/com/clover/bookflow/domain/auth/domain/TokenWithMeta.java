package com.clover.bookflow.domain.auth.domain;

import java.time.Instant;

public record TokenWithMeta(
    String token,
    String jti,
    Instant expiresAt
) {

  public static TokenWithMeta of(String token, String jti, Instant expiresAt) {
    return new TokenWithMeta(token, jti, expiresAt);
  }

}
