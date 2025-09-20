package com.clover.bookflow.domain.book.dto;

import java.util.Set;

public record BookClassificationDto(
    Set<BookSaveRequestDto> existing,
    Set<BookSaveRequestDto> notExisting
) {

  public static BookClassificationDto of(Set<BookSaveRequestDto> existing,
      Set<BookSaveRequestDto> notExisting) {
    return new BookClassificationDto(existing, notExisting);
  }

}
