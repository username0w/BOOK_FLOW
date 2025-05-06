package com.clover.bookflow.global.exception;

import com.clover.bookflow.global.errorcode.ErrorCode;
import com.clover.bookflow.global.response.ApiResponse;
import com.clover.bookflow.global.response.ErrorDetails;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
      DuplicateResourceException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), List.of()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), List.of()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail("500", "서버 오류", List.of()));
  }


}
