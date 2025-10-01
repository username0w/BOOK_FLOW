package com.clover.bookflow.domain.blogpost.controller;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostCreateRequest;
import com.clover.bookflow.domain.blogpost.dto.request.BlogPostUpdateRequest;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostResponse;
import com.clover.bookflow.domain.blogpost.dto.response.BlogPostSimpleResponse;
import com.clover.bookflow.domain.blogpost.service.BlogPostService;
import com.clover.bookflow.global.response.ApiResponse;
import com.clover.bookflow.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ApiResponse<BlogPostResponse>> createBlogPost(
      @Valid @RequestBody BlogPostCreateRequest request,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {
    System.out.println("Request: " + request);
    System.out.println(
        "User ID: " + (userDetails != null ? userDetails.getId() : "userDetails is null"));
    System.out.println(
        "User Role: " + userDetails.getAuthorities()
    );

    BlogPostResponse blogPostResponse = blogPostService.createBlogPost(request,
        userDetails.getId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.created(blogPostResponse));
  }

  @PreAuthorize("hasPermission(#postId, 'Blog_Post', 'READ')")
  @GetMapping("/{postId}")
  public ResponseEntity<ApiResponse<BlogPostResponse>> getBlogPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {

    BlogPostResponse response = blogPostService.getBlogPost(postId, userDetails.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/public")
  public ResponseEntity<ApiResponse<PageResponse<BlogPostSimpleResponse>>> getPublicPosts(
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<BlogPostSimpleResponse> page = blogPostService.getPublicBlogPosts(pageable);
    PageResponse<BlogPostSimpleResponse> pageResponse = PageResponse.from(page);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/author/{authorId}")
  public ResponseEntity<ApiResponse<PageResponse<BlogPostSimpleResponse>>> getPostsByAuthor(
      @PathVariable Long authorId,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<BlogPostSimpleResponse> page = blogPostService.getPublicPostsByAuthor(authorId, pageable);
    PageResponse<BlogPostSimpleResponse> pageResponse = PageResponse.from(page);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<PageResponse<BlogPostSimpleResponse>>> getMyPosts(
      @AuthenticationPrincipal CustomMemberDetails userDetails,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<BlogPostSimpleResponse> page = blogPostService.getMyBlogPosts(userDetails.getId(),
        pageable);
    PageResponse<BlogPostSimpleResponse> pageResponse = PageResponse.from(page);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @PreAuthorize("hasPermission(#postId, 'Blog_Post', 'WRITE')")
  @PutMapping("/{postId}")
  public ResponseEntity<ApiResponse<BlogPostResponse>> updateBlogPost(
      @PathVariable Long postId,
      @Valid @RequestBody BlogPostUpdateRequest request,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {

    BlogPostResponse response = blogPostService.updateBlogPost(postId, request,
        userDetails.getId());
    System.out.println("blogPostResponse = " + response);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PreAuthorize("hasPermission(#postId, 'Blog_Post', 'DELETE')")
  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> deleteBlogPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal CustomMemberDetails userDetails) {

    blogPostService.deleteBlogPost(postId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }


}
