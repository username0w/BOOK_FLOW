package com.clover.bookflow.domain.blogpost.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clover.bookflow.domain.blogpost.enums.BlogPostState;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookTestHelper;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.global.errorcode.BlogPostErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlogPostTest {

  private Member author;
  private Member admin;
  private Member otherUser;

  @BeforeEach
  void setUp() {
    author = MemberTestHelper.createTestUser();
    admin = MemberTestHelper.createTestAdmin();
    otherUser = MemberTestHelper.createTestOtherUser();
  }

  @Test
  void testCreateDraft() {
    BlogPost post = BlogPost.createDraft("Title", "Content", author);

    assertThat(post.getTitle()).isEqualTo("Title");
    assertThat(post.getContent()).isEqualTo("Content");
    assertThat(post.getState()).isEqualTo(BlogPostState.DRAFT);
    assertThat(post.getAuthor()).isEqualTo(author);
  }

  @Test
  void testCreatePublished() {
    BlogPost post = BlogPost.createPublished("Title", "Content", author);

    assertThat(post.getState()).isEqualTo(BlogPostState.ACTIVE);
  }

  @Test
  void testUpdateContent() {
    BlogPost post = BlogPost.createDraft("Old Title", "Old Content", author);
    post.update("New Title", "New Content");

    assertThat(post.getTitle()).isEqualTo("New Title");
    assertThat(post.getContent()).isEqualTo("New Content");
  }

  @Test
  void testUpdateDeletedPost_throwsException() {
    BlogPost post = BlogPostTestHelper.createDraftPost(author);
    post.delete();

    assertThatThrownBy(() -> post.update("X", "Y"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("삭제된 글은 수정할 수 없습니다.");
  }

  @Test
  void testStateTransitions() {
    BlogPost post = BlogPostTestHelper.createDraftPost(author);

    post.publish();
    assertThat(post.getState()).isEqualTo(BlogPostState.ACTIVE);

    post.makePrivate();
    assertThat(post.getState()).isEqualTo(BlogPostState.PRIVATE);

    post.delete();
    assertThat(post.getState()).isEqualTo(BlogPostState.DELETED);
  }

  @Test
  void testInvalidStateTransition_throwsException() {
    BlogPost post = BlogPostTestHelper.createDraftPost(author);
    post.delete();

    assertThatThrownBy(post::publish)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("상태 전환 불가");
  }

  @Test
  void testAddAndRemoveBooks() {
    BlogPost post = BlogPostTestHelper.createDraftPost(author);
    Book book1 = BookTestHelper.createBookWithDetail();
    Book book2 = BookTestHelper.createBook();

    post.addBook(book1);
    post.addBook(book1);
    assertThat(post.getBlogPostBooks()).hasSize(1);

    post.addBook(book2);
    assertThat(post.getBlogPostBooks()).hasSize(2);

    post.removeBook(book1);
    assertThat(post.getBlogPostBooks()).hasSize(1);
  }

  @Test
  void testIsAccessibleBy() {
    BlogPost post = BlogPost.createDraft("Title", "Content", author);

    // 작성자와 관리자 접근 가능
    assertThat(post.isAccessibleBy(author)).isTrue();
    assertThat(post.isAccessibleBy(admin)).isTrue();

    // 일반 유저는 DRAFT 접근 불가
    assertThat(post.isAccessibleBy(otherUser)).isFalse();

    // 상태를 ACTIVE로 바꾸면 모든 사용자 접근 가능
    post.publish();
    assertThat(post.isAccessibleBy(otherUser)).isTrue();
  }

  @Test
  void testValidateAccessibleBy_throwsException() {
    BlogPost post = BlogPost.createDraft("Title", "Content", author);

    assertThatThrownBy(() -> post.validateAccessibleBy(otherUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage(BlogPostErrorCode.UNAUTHORIZED_ACCESS.getMessage());
  }

  @Test
  void testIsXXXMethods() {
    BlogPost post = BlogPost.createDraft("Title", "Content", author);
    assertThat(post.isDraft()).isTrue();

    post.publish();
    assertThat(post.isActive()).isTrue();

    post.makePrivate();
    assertThat(post.isPrivate()).isTrue();

    post.delete();
    assertThat(post.isDeleted()).isTrue();
  }
}
