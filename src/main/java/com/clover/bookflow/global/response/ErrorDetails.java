package com.clover.bookflow.global.response;

public record ErrorDetails(
    String field,
    String message
) {

  public static ErrorDetails of(String field, String message) {
    return new ErrorDetails(field, message);
  }

}