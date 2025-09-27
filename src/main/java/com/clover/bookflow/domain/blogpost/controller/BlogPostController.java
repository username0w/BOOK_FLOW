package com.clover.bookflow.domain.blogpost.controller;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostCreateRequest;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostUpdateRequest;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostResponse;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostSimpleResponse;
import com.clover.bookflow.domain.blogpost.service.BlogPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blog-posts")
@RequiredArgsConstructor
public class BlogPostController {

  private final BlogPostService blogPostService;

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping
  public BlogPostResponse createBlogPost(
      @Valid @RequestBody BlogPostCreateRequest request,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {
    return blogPostService.createBlogPost(request, userDetails.getId());
  }

  @PreAuthorize("@blogPostPermissionEvaluator.hasPermission(authentication, #postId, 'READ')")
  @GetMapping("/{postId}")
  public BlogPostResponse getBlogPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {
    return blogPostService.getBlogPost(postId, userDetails != null ? userDetails.getId() : null);
  }

  @GetMapping("/public")
  public Page<BlogPostSimpleResponse> getPublicPosts(
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    return blogPostService.getPublicBlogPosts(pageable);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // 필요한 권한만 넣으세요
  @GetMapping("/author/{authorId}")
  public Page<BlogPostSimpleResponse> getPostsByAuthor(
      @PathVariable Long authorId,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    return blogPostService.getPublicPostsByAuthor(authorId, pageable);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public Page<BlogPostSimpleResponse> getMyPosts(
      @AuthenticationPrincipal CustomMemberDetails userDetails,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    return blogPostService.getMyBlogPosts(userDetails.getId(), pageable);
  }

  @PreAuthorize("@blogPostPermissionEvaluator.hasPermission(authentication, #postId, 'WRITE')")
  @PutMapping("/{postId}")
  public BlogPostResponse updateBlogPost(
      @PathVariable Long postId,
      @Valid @RequestBody BlogPostUpdateRequest request,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {
    return blogPostService.updateBlogPost(postId, request, userDetails.getId());
  }

  @PreAuthorize("@blogPostPermissionEvaluator.hasPermission(authentication, #postId, 'DELETE')")
  @DeleteMapping("/{postId}")
  public void deleteBlogPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {
    blogPostService.deleteBlogPost(postId, userDetails.getId());
  }

}
