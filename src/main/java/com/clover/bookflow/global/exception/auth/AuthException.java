package com.clover.bookflow.global.exception.auth;

import com.clover.bookflow.global.errorcode.ErrorCode;
import com.clover.bookflow.global.exception.CustomException;
import lombok.Getter;

@Getter
public class AuthException extends CustomException {
  
  public AuthException(ErrorCode errorCode) {
    super(errorCode);
  }
}
