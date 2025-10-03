package com.clover.bookflow.domain.blogpost.dto.response;

import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import java.time.LocalDateTime;

public record BlogPostSimpleResponse(
    Long id,
    String title,
    String summary,
    String authorName,
    LocalDateTime createdAt
) {

  public static BlogPostSimpleResponse from(BlogPost blogPost) {
    // summary는 content 일부를 잘라서 만듦 (100자 제한)
    String summary = blogPost.getContent().length() > 100
        ? blogPost.getContent().substring(0, 100) + "..."
        : blogPost.getContent();

    return new BlogPostSimpleResponse(
        blogPost.getId(),
        blogPost.getTitle(),
        summary,
        blogPost.getAuthor().getNickname(),
        blogPost.getCreatedAt()
    );
  }
}
