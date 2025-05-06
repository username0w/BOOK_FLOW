package com.clover.bookflow.global.errorcode;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
  // User
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 등록된 이메일입니다."),
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXISTS", "이미 존재하는 닉네임입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  UserErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  @Override
  public HttpStatus getStatus() {
    return status;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
