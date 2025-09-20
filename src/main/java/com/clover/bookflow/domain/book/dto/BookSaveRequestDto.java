package com.clover.bookflow.domain.book.dto;

import com.clover.bookflow.domain.book.entity.Book;
import java.time.LocalDate;

public record BookSaveRequestDto(
    String isbn,
    String title,
    String author,
    String publisher,
    String coverImgUrl,
    LocalDate publishedDate,
    String description,
    String category
) {

  public Book toEntity() {
    return new Book(isbn, title, author, publisher, coverImgUrl, publishedDate, description,
        category);
  }
}
