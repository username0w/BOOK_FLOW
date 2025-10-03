package com.clover.bookflow.domain.blogpost.repository;

import com.clover.bookflow.domain.blogpost.entity.BlogPost;
import com.clover.bookflow.domain.blogpost.enums.BlogPostState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // JPA 면 안붙여도 인식함
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

  // 상태가 "DELETE" 가 아닌 글만 조회
  Optional<BlogPost> findByIdAndStateNot(Long id, BlogPostState state);

  // 전체 글 목록에서 "ACTIVE" 상태만 조회 (예: ACTIVE)
  Page<BlogPost> findByState(BlogPostState state, Pageable pageable);

  // 특정 작성자의 글 중 상태가 "ACTIVE" 인 글 조회
  Page<BlogPost> findByAuthorIdAndState(Long authorId, BlogPostState state, Pageable pageable);

  // 나의 글 중 상태가 "DELETED"가 아닌 글 조회
  Page<BlogPost> findByAuthorIdAndStateNot(Long authorId, BlogPostState state, Pageable pageable);

}

