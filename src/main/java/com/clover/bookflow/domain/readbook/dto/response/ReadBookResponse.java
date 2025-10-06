package com.clover.bookflow.domain.readbook.dto.response;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import java.time.LocalDate;

public record ReadBookResponse(
    Long readBookId,
    Long memberId,
    Long bookId,
    String title,
    String author,
    String coverImgUrl,
    LocalDate readDate
) {

  public static ReadBookResponse from(ReadBook readBook) {
    Book book = readBook.getBook();
    return new ReadBookResponse(
        readBook.getId(),
        readBook.getMember().getId(),
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getCoverImgUrl(),
        readBook.getReadDate()
    );
  }
}
