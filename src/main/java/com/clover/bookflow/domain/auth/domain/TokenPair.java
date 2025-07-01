package com.clover.bookflow.domain.auth.domain;

public record TokenPair(
    TokenWithMeta accessToken,
    TokenWithMeta refreshToken
) {
  public static TokenPair of(final TokenWithMeta accessToken, final TokenWithMeta refreshToken) {
    return new TokenPair(accessToken, refreshToken);
  }
}
