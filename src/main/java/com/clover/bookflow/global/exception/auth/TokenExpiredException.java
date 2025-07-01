package com.clover.bookflow.global.exception.auth;

import com.clover.bookflow.global.errorcode.TokenErrorCode;
import lombok.Getter;

@Getter
public class TokenExpiredException extends AuthException {

  public TokenExpiredException() {
    super(TokenErrorCode.TOKEN_EXPIRED);
  }

}
