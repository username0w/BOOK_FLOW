package com.clover.bookflow.domain.blogpost.dto.request;

import java.util.List;

public record BlogPostUpdateRequest(

    String title,
    String content,
    List<Long> bookIds
) {

}
