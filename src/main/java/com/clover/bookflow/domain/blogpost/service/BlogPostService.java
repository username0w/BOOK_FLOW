package com.clover.bookflow.domain.blogpost.service;

import com.clover.bookflow.domain.blogpost.dto.request.BlogPostCreateRequest;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostUpdateRequest;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostResponse;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostSimpleResponse;
import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import com.clover.bookflow.domain.blogpost.enums.BlogPostState;
import com.clover.bookflow.domain.blogpost.repository.BlogPostRepository;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.repository.BookRepository;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.errorcode.BlogPostErrorCode;
import com.clover.bookflow.global.errorcode.BookErrorCode;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPostService {

  private final BlogPostRepository blogPostRepository;
  private final MemberRepository memberRepository;
  private final BookRepository bookRepository;

  // 블로그 글 작성
  @Transactional
  public BlogPostResponse createBlogPost(BlogPostCreateRequest dto, Long memberId) {
    Member author = findMemberById(memberId);

    // 블로그 글 생성
    BlogPost blogPost = BlogPost.createDraft(dto.title(), dto.content(), author);

    // 책 목록 확인
    List<Book> books = bookRepository.findAllById(dto.bookIds());
    if (books.size() != dto.bookIds().size()) {
      throw new BusinessException(BookErrorCode.BOOK_NOT_FOUND);
    }

    blogPost.addBooks(books);
    blogPostRepository.save(blogPost);

    return BlogPostResponse.from(blogPost);
  }

  // 블로그 글 단건 조회
  public BlogPostResponse getBlogPost(Long postId, Long memberId) {
    // DB에서 status가 DELETED인 글 제외
    BlogPost blogPost = findBlogPostByIdAndStateNotDeleted(postId);

    // 권한 체크
    if (!canViewBlogPost(blogPost, memberId)) {
      throw new BusinessException(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
    }

    return BlogPostResponse.from(blogPost);
  }

  // 블로그 글 수정
  @Transactional
  public BlogPostResponse updateBlogPost(Long postId, BlogPostUpdateRequest dto, Long memberId) {
    BlogPost blogPost = findBlogPostByIdAndStateNotDeleted(postId);

    // 권한 체크
    hasPermission(blogPost, memberId);

    // 블로그 글 업데이트
    blogPost.update(dto.title(), dto.content());

    return BlogPostResponse.from(blogPost);
  }

  // 블로그 글 삭제
  @Transactional
  public void deleteBlogPost(Long postId, Long memberId) {
    BlogPost blogPost = findBlogPostByIdAndStateNotDeleted(postId);

    // 권한 체크
    if (!hasPermission(blogPost, memberId)) {
      throw new BusinessException(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 글 삭제 처리
    blogPost.delete();
  }

  // 활성화된 글과 비활성화된 글을 구분하는 메서드
  private boolean canViewBlogPost(BlogPost blogPost, Long memberId) {
    if (blogPost.isActive()) {
      return true; // 활성화된 글은 누구나 볼 수 있음
    }
    return hasPermission(blogPost, memberId); // 비활성화된 글은 작성자나 관리자만 볼 수 있음
  }

  // 권한 확인 메서드
  private boolean hasPermission(BlogPost blogPost, Long memberId) {
    if (isAuthor(blogPost, memberId) || isMemberAdmin(memberId)) {
      return true;
    }
    throw new BusinessException(BlogPostErrorCode.UNAUTHORIZED_ACCESS);
  }

  // 블로그 글 작성자 확인
  private boolean isAuthor(BlogPost blogPost, Long memberId) {
    return blogPost.getAuthor().getId().equals(memberId);
  }

  // 관리자인지 확인
  private boolean isMemberAdmin(Long memberId) {
    return memberRepository.findById(memberId)
        .map(Member::isAdmin)
        .orElse(false);
  }

  // BlogPost 객체 찾기
  private BlogPost findBlogPostByIdAndStateNotDeleted(Long id) {
    // 삭제된 것은 조회 안됨.
    return blogPostRepository.findByIdAndStateNot(id, BlogPostState.DELETED)
        .orElseThrow(() -> new BusinessException(BlogPostErrorCode.BLOG_POST_NOT_FOUND));
  }

  // Member 객체 찾기
  private Member findMemberById(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
  }

  // 블로그 글 목록 조회 (전체)
  public Page<BlogPostSimpleResponse> getPublicBlogPosts(Pageable pageable) {
    Page<BlogPost> posts = blogPostRepository.findByState(BlogPostState.ACTIVE,
        pageable);
    return posts.map(BlogPostSimpleResponse::from);
  }

  // 작성자 페이지에서의 글 목록 조회
  public Page<BlogPostSimpleResponse> getPublicPostsByAuthor(Long authorId, Pageable pageable) {
    Page<BlogPost> posts = blogPostRepository.findByAuthorIdAndState(authorId,
        BlogPostState.ACTIVE, pageable);
    return posts.map(BlogPostSimpleResponse::from);
  }

  // 내 페이지에서의 글 목록 조회
  public Page<BlogPostSimpleResponse> getMyBlogPosts(Long memberId, Pageable pageable) {
    Page<BlogPost> posts = blogPostRepository.findByAuthorIdAndStateNot(memberId,
        BlogPostState.DELETED, pageable);
    return posts.map(BlogPostSimpleResponse::from);
  }
}
