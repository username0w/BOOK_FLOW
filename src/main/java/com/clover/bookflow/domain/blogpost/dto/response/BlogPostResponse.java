package com.clover.bookflow.domain.blogpost.dto.response;

import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import com.clover.bookflow.domain.book.dto.BookInfoResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record BlogPostResponse(

    Long id,
    String title,
    String content,
    String authorName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<BookInfoResponse> books
) {

  public static BlogPostResponse from(BlogPost blogPost) {
    List<BookInfoResponse> bookResponses = blogPost.getBlogPostBooks().stream()
        .map(bpBook -> {
          var book = bpBook.getBook();
          return new BookInfoResponse(book.getId(), book.getTitle(), book.getAuthor());
        })
        .collect(Collectors.toList());

    return new BlogPostResponse(
        blogPost.getId(),
        blogPost.getTitle(),
        blogPost.getContent(),
        blogPost.getAuthor().getNickname(),
        blogPost.getCreatedAt(),
        blogPost.getUpdatedAt(),
        bookResponses
    );
  }
}
