package com.clover.bookflow.domain.blogpost.entity;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blogpost_books")
public class BlogPostBook extends BaseTimeEntity { // 블로그 글과 도서 연결, 중간 엔티티

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "blog_post_id", nullable = false)
  private BlogPost blogPost;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  public BlogPostBook(BlogPost blogPost, Book book) {
    this.blogPost = blogPost;
    this.book = book;
  }
}
