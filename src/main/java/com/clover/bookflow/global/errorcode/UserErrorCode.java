package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {

  USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_USER_STATE("USER_002", "사용자 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  LOGIN_FAILED("USER_003", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

  EMAIL_ALREADY_EXISTS("USER_004", "이미 등록된 이메일입니다.", HttpStatus.CONFLICT),
  NICKNAME_ALREADY_EXISTS("USER_005", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;

  UserErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

}
