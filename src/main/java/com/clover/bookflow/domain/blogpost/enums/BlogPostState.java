package com.clover.bookflow.domain.blogpost.enums;

public enum BlogPostState {
  
  ACTIVE,    // 정상 게시글
  DELETED,   // 논리 삭제된 글
  PRIVATE,   // 비공개 글
  DRAFT      // 임시 저장 등
}
