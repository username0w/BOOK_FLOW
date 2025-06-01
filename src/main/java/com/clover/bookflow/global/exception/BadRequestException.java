package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class BadRequestException extends CustomException {

  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }

}
