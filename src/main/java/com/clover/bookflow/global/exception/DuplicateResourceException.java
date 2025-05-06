package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

  private final ErrorCode errorCode;

  public DuplicateResourceException(final ErrorCode errorCode) { // 매개변수에 final?
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
