package com.clover.bookflow.domain.book.controller;

import com.clover.bookflow.domain.book.service.AdminBookIngestService;
import com.clover.bookflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/book")
@RequiredArgsConstructor
public class AdminBookController { // 관리 배치용

  // db 구축
  // 태깅 요청
  private final AdminBookIngestService adminBookIngestService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/ingest-bestsellers")
  public ResponseEntity<ApiResponse<String>> ingestBestSellers() {
    // bookadminserivce 에서 apiservice -> apiclient 호출?
    adminBookIngestService.ingestBestSellers();
    return ResponseEntity.ok().body(ApiResponse.success("베스트셀러 도서가 정상적으로 적재되었습니다."));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/ingest-recommended")
  public ResponseEntity<ApiResponse<String>> ingestRecommended() {
    adminBookIngestService.ingestRecommendedBooks();
    return ResponseEntity.ok().body(ApiResponse.success("추천 도서가 정상적으로 적재되었습니다."));
  }


}
