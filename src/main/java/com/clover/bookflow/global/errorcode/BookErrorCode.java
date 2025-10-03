package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {

  BOOK_NOT_FOUND("BOOK_001", "존재하지 않는 도서입니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_BOOK("BOOK_002", "이미 존재하는 도서입니다.", HttpStatus.CONFLICT),
  INVALID_BOOK_DATA("BOOK_003", "도서 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED_ACCESS("BOOK_004", "도서에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus status;

  BookErrorCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.status = httpStatus;
  }

}
