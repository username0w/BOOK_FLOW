package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TokenErrorCode implements ErrorCode {

  TOKEN_EXPIRED("TOKEN_001", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
  TOKEN_NOT_FOUND("TOKEN_002", "토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_TOKEN("TOKEN_003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
  ILLEGAL_TOKEN_USE("TOKEN_004", "잘못된 토큰 사용입니다.", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus status;

  TokenErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

}
