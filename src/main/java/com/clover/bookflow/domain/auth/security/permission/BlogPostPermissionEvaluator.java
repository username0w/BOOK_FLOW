package com.clover.bookflow.domain.blogpost.security;

import com.clover.bookflow.domain.auth.security.permission.DomainPermissionEvaluator;
import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import com.clover.bookflow.domain.blogpost.repository.BlogPostRepository;
import com.clover.bookflow.domain.member.entity.Member;
import java.io.Serializable;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlogPostPermissionEvaluator implements DomainPermissionEvaluator {

  private final BlogPostRepository blogPostRepository;

  @Override
  public boolean supports(String targetType) {
    return "BLOG_POST".equalsIgnoreCase(targetType);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String permission) {
    if (authentication == null || targetId == null || permission == null) {
      return false;
    }

    Member currentUser = (Member) authentication.getPrincipal();

    Optional<BlogPost> blogPostOpt = blogPostRepository.findById((Long) targetId);
    if (blogPostOpt.isEmpty()) {
      return false;
    }

    BlogPost blogPost = blogPostOpt.get();

    return switch (permission.toUpperCase()) {
      case "READ" -> blogPost.isAccessibleBy(currentUser);
      case "WRITE" -> blogPost.getAuthor().equals(currentUser) || currentUser.isAdmin();
      case "DELETE" -> blogPost.getAuthor().equals(currentUser) || currentUser.isAdmin();
      default -> false;
    };
  }
}
