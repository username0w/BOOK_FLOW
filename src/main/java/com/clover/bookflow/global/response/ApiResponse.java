package com.clover.bookflow.global.response;

import java.util.List;

public record ApiResponse<T>(
    boolean success,
    String code,
    String message,
    T data,
    List<ErrorDetails> errors
) {
  // 생성자 자동 제공

  // success
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "201", "요청을 성공적으로 보냈습니다.", data, List.of());
  }


  public static <T> ApiResponse<T> success(String code, String message, T data) {
    return new ApiResponse<>(true, code, message, data, List.of());
  }

  // failure
  public static <T> ApiResponse<T> fail(String code, String message, List<ErrorDetails> errors) {
    return new ApiResponse<>(false, code, message, null, errors);
  }

}
