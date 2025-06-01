package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends CustomException {

  public UnauthorizedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
