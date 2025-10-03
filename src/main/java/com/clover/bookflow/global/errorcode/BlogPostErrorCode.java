package com.clover.bookflow.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BlogPostErrorCode implements ErrorCode {

  BLOG_POST_NOT_FOUND("BLOGPOST_001", "해당 블로그 글이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  UNAUTHORIZED_ACCESS("BLOGPOST_002", "블로그 글에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
  INVALID_BLOG_POST_STATE("BLOGPOST_003", "블로그 글의 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  DELETED_BLOG_POST_ACCESS("BLOGPOST_005", "삭제된 블로그 글에는 접근할 수 없습니다.", HttpStatus.GONE),
  DUPLICATE_TITLE("BLOGPOST_004", "이미 존재하는 블로그 글 제목입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;

  BlogPostErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }
}
