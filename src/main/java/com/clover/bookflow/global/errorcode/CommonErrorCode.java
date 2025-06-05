package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {

  DUPLICATE_RESOURCE("COMMON_001", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
  INVALID_INPUT("COMMON_002", "입력값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("COMMON_003", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("COMMON_004", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  INTERNAL_ERROR("COMMON_005", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;

  CommonErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

}
