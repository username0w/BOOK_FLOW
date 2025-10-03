package com.clover.bookflow.global.response;

public record Pagination(
    int page,
    int size,
    int totalPages,
    long totalElements,
    boolean first,
    boolean last
) {

}
