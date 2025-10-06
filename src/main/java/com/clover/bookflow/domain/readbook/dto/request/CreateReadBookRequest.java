package com.clover.bookflow.domain.readbook.dto.request;

import java.time.LocalDate;

public record CreateReadBookRequest(
    String isbn,
    String title,
    String author,
    String publisher,
    String publishedDate,
    String coverUrl,
    LocalDate readDate
) {

}
