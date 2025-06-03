package com.clover.bookflow.global.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  String getCode();

  String getMessage();

  HttpStatus getStatus();

}
