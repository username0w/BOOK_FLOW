package com.clover.bookflow.global.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    Pagination pagination
) {

  public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        new Pagination(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast()
        )
    );
  }
}
