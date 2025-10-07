package com.clover.bookflow.domain.book.dto;

import java.time.LocalDate;

public record BookSearchResponse(
    String isbn,
    String title,
    String author,
    String publisher,
    String coverImgUrl,
    LocalDate publishedDate,
    String category
) {

}
