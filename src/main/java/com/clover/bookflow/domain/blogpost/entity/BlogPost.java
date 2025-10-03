package com.clover.bookflow.domain.blogpost.entity;

import com.clover.bookflow.domain.blogpost.enums.BlogPostState;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.common.AuthorIdentifiable;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.global.common.BaseTimeEntity;
import com.clover.bookflow.global.errorcode.BlogPostErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blog_posts")
public class BlogPost extends BaseTimeEntity implements AuthorIdentifiable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Lob
  @Column(nullable = false)
  private String content; // Markdown or HTML 형식 저장

  @Column(nullable = false)
  private BlogPostState state = BlogPostState.DRAFT;

  // 여러 BlogPost는 한 명의 Member(작성자)에 속함
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private Member author;

  // 하나의 BlogPost는 여러 BlogPostBook(도서 연결)을 가질 수 있음
  @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BlogPostBook> blogPostBooks = new ArrayList<>();

  // 생성자
  private BlogPost(String title, String content, Member author, BlogPostState state) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.state = state;
  }

  public static BlogPost createDraft(String title, String content, Member author) {
    return new BlogPost(title, content, author, BlogPostState.DRAFT);
  }

  public static BlogPost createPublished(String title, String content, Member author) {
    return new BlogPost(title, content, author, BlogPostState.ACTIVE);
  }

  public void update(String newTitle, String newContent) {
    if (this.state == BlogPostState.DELETED) {
      throw new IllegalStateException("삭제된 글은 수정할 수 없습니다.");
    }
    this.title = newTitle;
    this.content = newContent;
    // 필요하면 수정 시간 갱신 등 추가
  }

  public void makePrivate() {
    changeState(BlogPostState.PRIVATE);
  }

  public void publish() {
    changeState(BlogPostState.ACTIVE);
  }

  public void delete() {
    changeState(BlogPostState.DELETED);
  }

  public void saveAsDraft() {
    changeState(BlogPostState.DRAFT);
  }

  private void changeState(BlogPostState newState) {
    if (!canTransitionTo(newState)) {
      throw new IllegalStateException("상태 전환 불가: " + this.state + " → " + newState);
    }
    this.state = newState;
  }

  private boolean canTransitionTo(BlogPostState newState) {
    return switch (this.state) {
      case DRAFT -> newState == BlogPostState.ACTIVE || newState == BlogPostState.PRIVATE
          || newState == BlogPostState.DELETED;
      case ACTIVE -> newState == BlogPostState.PRIVATE || newState == BlogPostState.DELETED;
      case PRIVATE -> newState == BlogPostState.ACTIVE || newState == BlogPostState.DELETED;
      case DELETED -> false;
    };
  }

  public void addBook(Book book) {
    boolean alreadyLinked = blogPostBooks.stream()
        .anyMatch(bpb -> bpb.getBook().equals(book));

    if (alreadyLinked) {
      return;
    }

    BlogPostBook blogPostBook = new BlogPostBook(this, book);
    this.blogPostBooks.add(blogPostBook);
  }

  public void addBooks(List<Book> books) {
    if (books == null || books.isEmpty()) {
      return;
    }
    for (Book book : books) {
      addBook(book);
    }
  }

  public void removeBook(Book book) {
    blogPostBooks.removeIf(bpb -> bpb.getBook().equals(book));
  }

  // 상태 관련
  public boolean isDeleted() {
    return this.state == BlogPostState.DELETED;
  }

  public boolean isPrivate() {
    return this.state == BlogPostState.PRIVATE;
  }

  public boolean isDraft() {
    return this.state == BlogPostState.DRAFT;
  }

  public boolean isActive() {
    return this.state == BlogPostState.ACTIVE;
  }

  public boolean isAccessibleBy(Member viewer) {
    if (this.isDeleted()) {
      return false;
    }

    boolean isOwner = this.author.equals(viewer);
    boolean isAdmin = viewer != null && viewer.isAdmin();

    if (this.isPrivate() || this.isDraft()) {
      return isOwner || isAdmin;
    }

    return true;
  }

  public void validateAccessibleBy(Member viewer) {
    if (!isAccessibleBy(viewer)) {
      throw new BusinessException(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  @Override
  public Long getAuthorId() {
    return author != null ? author.getId() : null;
  }
}
