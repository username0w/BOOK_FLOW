package com.clover.bookflow.domain.readbook.dto.request;

import java.time.LocalDate;

public record CreateReadBookRequest(
    String isbn,
    LocalDate readDate
) {

}
