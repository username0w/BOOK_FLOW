package com.clover.bookflow.global.exception.auth;

import com.clover.bookflow.global.errorcode.ErrorCode;

public class InvalidTokenException extends AuthException {

  public InvalidTokenException(ErrorCode errorCode) {
    super(errorCode);
  }

}
