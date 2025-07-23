package com.clover.bookflow.global.exception.handler;

import com.clover.bookflow.global.errorcode.ErrorCode;
import com.clover.bookflow.global.exception.CustomException;
import com.clover.bookflow.global.response.ApiResponse;
import com.clover.bookflow.global.response.ErrorDetails;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Void>> handleCustomExceptions(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {

    List<ErrorDetails> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> ErrorDetails.of(error.getField(), error.getDefaultMessage()))
        .toList();

    return ResponseEntity.badRequest().body(ApiResponse.fail("400", "입력값 오류", errors));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    log.error("Unhandled exception caught in global handler", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail("500", "서버 오류", List.of()));
  }

}
