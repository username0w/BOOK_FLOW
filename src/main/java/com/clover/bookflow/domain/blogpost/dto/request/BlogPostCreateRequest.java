package com.clover.bookflow.domain.blogpost.dto.request;

import java.util.List;

public record BlogPostCreateRequest(
    String title,
    String content,
    List<Long> bookIds
) {

}