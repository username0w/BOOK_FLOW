package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends CustomException {

  public BusinessException(ErrorCode errorCode) {
    super(errorCode);
  }
}
