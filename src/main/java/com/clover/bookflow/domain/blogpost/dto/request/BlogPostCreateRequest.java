package com.clover.bookflow.domain.blogpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record BlogPostCreateRequest(
    @NotBlank String title,
    @NotBlank String content,
    List<Long> bookIds
) {

}