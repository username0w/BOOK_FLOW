package com.clover.bookflow.domain.readbook.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReadBookRequest(

        @NotNull LocalDate readDate
) {
}
