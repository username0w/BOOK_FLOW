package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

  private final ErrorCode errorCode;

  public BadRequestException(final ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
