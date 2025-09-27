package com.clover.bookflow.domain.blogpost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.blogpost.dto.request.BlogPostCreateRequest;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostUpdateRequest;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostResponse;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostSimpleResponse;
import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import com.clover.bookflow.domain.blogpost.enums.BlogPostState;
import com.clover.bookflow.domain.blogpost.repository.BlogPostRepository;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookTestHelper;
import com.clover.bookflow.domain.book.repository.BookRepository;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.errorcode.BlogPostErrorCode;
import com.clover.bookflow.global.errorcode.BookErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

  @Mock
  private BlogPostRepository blogPostRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BlogPostService blogPostService;

  private Member author;

  @BeforeEach
  void setUp() {
    author = MemberTestHelper.createTestUser();
  }

  @Nested
  class CreateTests {

    @Test
    void createBlogPost_success() {
      // given
      BlogPostCreateRequest createRequest = new BlogPostCreateRequest("Test Title", "Test Content",
          List.of(1L, 2L));
      given(memberRepository.findById(1L)).willReturn(Optional.of(author));

      List<Book> books = BookTestHelper.mockBooks(1L, 2L);
      given(bookRepository.findAllById(createRequest.bookIds())).willReturn(books);

      // when
      BlogPostResponse response = blogPostService.createBlogPost(createRequest, 1L);

      // then
      assertThat(response.title()).isEqualTo("Test Title");
      assertThat(response.content()).isEqualTo("Test Content");
      verify(blogPostRepository).save(any(BlogPost.class));
    }

    @Test
    void createBlogPost_whenBookMissing_thenThrow() {
      BlogPostCreateRequest req = new BlogPostCreateRequest("Title", "Content", List.of(1L, 2L));
      given(memberRepository.findById(1L)).willReturn(Optional.of(author));

      List<Book> foundBooks = List.of(BookTestHelper.createBook());
      given(bookRepository.findAllById(req.bookIds())).willReturn(foundBooks);

      assertThatThrownBy(() -> blogPostService.createBlogPost(req, 1L))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorCode()).isEqualTo(BookErrorCode.BOOK_NOT_FOUND);
          });

      verify(blogPostRepository, never()).save(any());
    }
  }

  @Nested
  class ReadTests {

    @Test
    void getBlogPost_success_whenActive() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      given(blogPost.getAuthor()).willReturn(author);

      BlogPostService spyService = spy(blogPostService);
      doReturn(true).when(spyService).canViewBlogPost(blogPost, 1L);

      given(blogPost.getTitle()).willReturn("Title X");
      given(blogPost.getContent()).willReturn("Content X");

      BlogPostResponse resp = spyService.getBlogPost(1L, 1L);

      assertThat(resp.title()).isEqualTo("Title X");
      assertThat(resp.content()).isEqualTo("Content X");
      verify(blogPostRepository).findByIdAndStateNot(1L, BlogPostState.DELETED);
    }

    @Test
    void getBlogPost_whenUnauthorized_thenThrow() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      BlogPostService spyService = spy(blogPostService);
      doReturn(false).when(spyService).canViewBlogPost(blogPost, 1L);

      assertThatThrownBy(() -> spyService.getBlogPost(1L, 1L))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorCode()).isEqualTo(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
          });
    }

    @Test
    void getBlogPost_whenNotFound_thenThrow() {
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> blogPostService.getBlogPost(1L, 1L))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorCode()).isEqualTo(BlogPostErrorCode.BLOG_POST_NOT_FOUND);
          });
    }
  }

  @Nested
  class UpdateTests {

    @Test
    void updateBlogPost_success() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      given(blogPost.getAuthor()).willReturn(author);

      BlogPostUpdateRequest dto = new BlogPostUpdateRequest("New Title", "New Content", List.of());
      BlogPostService spyService = spy(blogPostService);
      doReturn(true).when(spyService).hasPermission(blogPost, 1L);

      given(blogPost.getTitle()).willReturn("New Title");
      given(blogPost.getContent()).willReturn("New Content");

      BlogPostResponse resp = spyService.updateBlogPost(1L, dto, 1L);

      assertThat(resp.title()).isEqualTo("New Title");
      assertThat(resp.content()).isEqualTo("New Content");
    }

    @Test
    void updateBlogPost_whenNoPermission_thenThrow() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      BlogPostService spyService = spy(blogPostService);
      doReturn(false).when(spyService).hasPermission(blogPost, 2L);

      BlogPostUpdateRequest dto = new BlogPostUpdateRequest("New Title", "New Content", List.of());

      assertThatThrownBy(() -> spyService.updateBlogPost(1L, dto, 2L))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorCode()).isEqualTo(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
          });
    }
  }

  @Nested
  class DeleteTests {

    @Test
    void deleteBlogPost_success() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      BlogPostService spyService = spy(blogPostService);
      doReturn(true).when(spyService).hasPermission(blogPost, 1L);

      spyService.deleteBlogPost(1L, 1L);

      verify(blogPost).delete();
    }

    @Test
    void deleteBlogPost_whenNoPermission_thenThrow() {
      BlogPost blogPost = mock(BlogPost.class);
      given(blogPostRepository.findByIdAndStateNot(1L, BlogPostState.DELETED))
          .willReturn(Optional.of(blogPost));

      BlogPostService spyService = spy(blogPostService);
      doReturn(false).when(spyService).hasPermission(blogPost, 1L);

      assertThatThrownBy(() -> spyService.deleteBlogPost(1L, 1L))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorCode()).isEqualTo(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
          });
    }
  }

  @Nested
  class QueryTests {

    @Test
    void getPublicBlogPosts_whenPostsExist_thenReturnList() {
      BlogPost post = BlogPost.createPublished("제목", "내용", author);
      Page<BlogPost> page = new PageImpl<>(List.of(post));
      given(blogPostRepository.findByState(BlogPostState.ACTIVE, Pageable.unpaged()))
          .willReturn(page);

      Page<BlogPostSimpleResponse> result = blogPostService.getPublicBlogPosts(Pageable.unpaged());

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().title()).isEqualTo("제목");
    }

    @Test
    void getPublicBlogPosts_empty() {
      Page<BlogPost> emptyPage = Page.empty();
      given(blogPostRepository.findByState(eq(BlogPostState.ACTIVE), any(Pageable.class)))
          .willReturn(emptyPage);

      Page<BlogPostSimpleResponse> result = blogPostService.getPublicBlogPosts(Pageable.unpaged());

      assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getPublicPostsByAuthor_whenPostsExist_thenReturnList() {
      BlogPost post = BlogPost.createPublished("작성자 글", "내용", author);
      Page<BlogPost> page = new PageImpl<>(List.of(post));
      given(blogPostRepository.findByAuthorIdAndState(1L, BlogPostState.ACTIVE, Pageable.unpaged()))
          .willReturn(page);

      Page<BlogPostSimpleResponse> result = blogPostService.getPublicPostsByAuthor(1L,
          Pageable.unpaged());

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().title()).isEqualTo("작성자 글");
    }

    @Test
    void getPublicPostsByAuthor_empty() {
      Page<BlogPost> emptyPage = Page.empty();
      given(
          blogPostRepository.findByAuthorIdAndState(eq(1L), eq(BlogPostState.ACTIVE),
              any(Pageable.class)))
          .willReturn(emptyPage);

      var result = blogPostService.getPublicPostsByAuthor(1L, Pageable.unpaged());

      assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getMyBlogPosts_whenPostsExist_thenReturnList() {
      BlogPost post = BlogPost.createPublished("내 글", "내용", author);
      Page<BlogPost> page = new PageImpl<>(List.of(post));
      given(
          blogPostRepository.findByAuthorIdAndStateNot(1L, BlogPostState.DELETED,
              Pageable.unpaged()))
          .willReturn(page);

      var result = blogPostService.getMyBlogPosts(1L, Pageable.unpaged());

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().title()).isEqualTo("내 글");
    }

    @Test
    void getMyBlogPosts_empty() {
      Page<BlogPost> emptyPage = Page.empty();
      given(blogPostRepository.findByAuthorIdAndStateNot(eq(1L), eq(BlogPostState.DELETED),
          any(Pageable.class)))
          .willReturn(emptyPage);

      var result = blogPostService.getMyBlogPosts(1L, Pageable.unpaged());

      assertThat(result.getContent()).isEmpty();
    }
  }
}
