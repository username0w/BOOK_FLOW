package com.clover.bookflow.global.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  HttpStatus getStatus();

  String getCode();

  String getMessage();

}
