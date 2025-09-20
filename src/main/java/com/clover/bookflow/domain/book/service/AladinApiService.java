package com.clover.bookflow.domain.book.service;

import com.clover.bookflow.domain.book.client.AladinApiClient;
import com.clover.bookflow.domain.book.dto.AladinResponseDto;
import com.clover.bookflow.domain.book.dto.BookSaveRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AladinApiService { //  클라이언트 호출 + 로직 + 예외 처리
  // 외부 내부 매핑

  private final AladinApiClient aladinApiClient;

  // Todo: 검색 (모든 도서 db 사전 저장은 한계 있다. db 에 없는 도서 검색 api 호출 후 개별 도서 저장 로직 구현)


  // 베스트셀러 리스트
  public List<BookSaveRequestDto> fetchBestSellers() {
    AladinResponseDto response = aladinApiClient.fetchBestSellers();

    if (response == null || response.items() == null || response.items().isEmpty()) {
      throw new RuntimeException("알라딘 응답이 비어있습니다.");
    }

    // 응답 변환 도서리스트
    return response.items().stream()
        .map(item -> new BookSaveRequestDto(
            item.isbn13(),
            item.title(),
            item.author(),
            item.publisher(),
            item.cover(),
            item.pubDate(),
            item.description(),
            item.categoryName()
        ))
        .toList();

  }


}
