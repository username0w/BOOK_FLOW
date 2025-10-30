package com.clover.bookflow.domain.readbook.controller;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.request.UpdateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.service.ReadBookService;
import com.clover.bookflow.global.response.ApiResponse;
import com.clover.bookflow.global.response.PageResponse;
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
@RequestMapping("/api/v1/read-books")
@RequiredArgsConstructor
public class ReadBookController {

    private final ReadBookService readBookService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReadBookResponse>> addReadBook(
            @RequestBody CreateReadBookRequest request,
            @AuthenticationPrincipal CustomMemberDetails userDetails) {

        ReadBookResponse readBookResponse = readBookService.addReadBook(request, userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(readBookResponse));
    }

    @PreAuthorize("hasPermission(#readBookId,'Read_Book', 'WRITE')")
    @PutMapping("/{readBookId}")
    public ResponseEntity<ApiResponse<ReadBookResponse>> updateReadBook(
            @PathVariable Long readBookId,
            @RequestBody UpdateReadBookRequest request,
            @AuthenticationPrincipal CustomMemberDetails userDetails) {

        ReadBookResponse readBookResponse = readBookService.updateReadBook(readBookId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(readBookResponse));
    }

    @PreAuthorize("hasPermission(#readBookId, 'Read_Book', 'DELETE')")
    @DeleteMapping("/{readBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteReadBook(
            @PathVariable Long readBookId,
            @AuthenticationPrincipal CustomMemberDetails userDetails) {

        readBookService.deleteReadBook(readBookId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReadBookResponse>>> getMyReadBooks(
            @AuthenticationPrincipal CustomMemberDetails userDetails,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReadBookResponse> page = readBookService.getReadBooksByMember(userDetails.getId(), pageable);
        PageResponse<ReadBookResponse> pageResponse = PageResponse.from(page);
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
}
