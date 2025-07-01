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
    return new ApiResponse<>(true, "200", "요청이 성공적으로 처리되었습니다.", data, List.of());
  }

  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(true, "201", "요청을 성공적으로 보냈습니다.", data, List.of());
  }

  public static <T> ApiResponse<T> noContent() {
    return new ApiResponse<>(true, "204", "요청이 성공적으로 처리되었으며 반환할 데이터가 없습니다.", null, List.of());
  }

  // failure
  public static <T> ApiResponse<T> fail(String code, String message, List<ErrorDetails> errors) {
    return new ApiResponse<>(false, code, message, null, errors);
  }

}
