package com.clover.bookflow.domain.book.controller;

import com.clover.bookflow.domain.book.dto.BookSearchResponse;
import com.clover.bookflow.domain.book.service.BookService;
import com.clover.bookflow.global.response.ApiResponse;
import com.clover.bookflow.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PageResponse<BookSearchResponse>>> searchBook(
      @RequestParam String keyword,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Page<BookSearchResponse> page = bookService.searchBooks(keyword, pageable);
    PageResponse<BookSearchResponse> pageResponse = PageResponse.from(page);

    return ResponseEntity.ok(ApiResponse.success(pageResponse));

  }

}
