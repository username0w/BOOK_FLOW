package com.clover.bookflow.domain.book.service;

import com.clover.bookflow.domain.book.dto.BookAllSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookClassificationDto;
import com.clover.bookflow.domain.book.dto.BookSaveRequestDto;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookIngestService {

  private final AladinApiService aladinApiService;

  private final NationalLibraryApiService nationalLibraryApiService;

  private final BookService bookService;

  private final BookDetailService bookDetailService;

  // 도서 데이터 저장
  // Todo: 스케줄러 적용
  public void ingestRecommendedBooks() { // NatLib

    List<BookAllSaveRequestDto> books = nationalLibraryApiService.fetchRecommendedBooks();

    // 중복, complete 제거

    bookService.saveBooksWithDetails(books);

  }

  public void ingestBestSellers() { // Aladin
    // 목록 저장용 api 호출
    // Book 에 없는 도서 필터링 -> Book 저장, BookDetail 저장
    // Book 에 있는 도서 && Complete 아닌 도서 필터링 -> BookDetail 저장

    List<BookSaveRequestDto> books = aladinApiService.fetchBestSellers();

    // 기본 호출된 목록 내 중복 제거 - 도서 등록 사용자가 안함. 중복 제거 관리자 몫

    // isbn 기준 Book 에 존재 유무 기준 분류
    BookClassificationDto filteredBooks = classifyBooksByExistence(books);
    // Book 에 없는 도서 필터링
    // 저장
    // 도서 데이터 저장
    bookService.saveBasicBooks(filteredBooks.notExisting());

    // Todo: 도서 상세 정보 데이터 미비 추후 보완 고려해서 보충
//    // Book 에 있지만 Complete 아닌 도서 필터링
//    // BookDetail 저장
//    List<BookSaveRequestDto> notCompletedBooks = removeCompletedBooks(filteredBooks.existing());
//
//    // notExisting, existingButNotComplete 둘 다 호출
//    List<BookSaveRequestDto> result = new ArrayList<>(filteredBooks.notExisting());
//    result.addAll(notCompletedBooks);
//
//    // 상세 정보 저장
//    // Book 에 없는 도서와 Book 에 있지만 Complete 아닌 도서
//    List<BookDetailSaveRequestDto> details = nationalLibraryApiService.fetchBookDetails(
//        result);
//
//    bookService.saveBookDetails(details);
  }

  // Todo: filter 필요 시
  private BookClassificationDto classifyBooksByExistence(List<BookSaveRequestDto> books) {
    Set<String> isbns = books.stream()
        .map(BookSaveRequestDto::isbn)
        .collect(Collectors.toSet());

    log.info("수신된 ISBN 목록: {}", isbns);

    Set<String> existingIsbns = bookService.findExistingIsbns(isbns);
    log.info("이미 존재하는 ISBN 목록: {}", existingIsbns);

    Map<Boolean, List<BookSaveRequestDto>> partitioned = books.stream()
        .collect(Collectors.partitioningBy(book -> existingIsbns.contains(book.isbn())));

    Set<BookSaveRequestDto> existingBooks = new HashSet<>(partitioned.get(true));
    Set<BookSaveRequestDto> notExistingBooks = new HashSet<>(partitioned.get(false));

    return BookClassificationDto.of(existingBooks, notExistingBooks);
  }


  // Todo: removeDuplicate
  private List<BookSaveRequestDto> removeCompletedBooks(Set<BookSaveRequestDto> books) {
    Set<String> completeIsbns = bookDetailService.findCompletedIsbns(
        books.stream().map(BookSaveRequestDto::isbn).collect(Collectors.toSet()));
    log.info("완료된 ISBN 목록: {}", completeIsbns);

    List<BookSaveRequestDto> filtered = books.stream()
        .filter(book -> !completeIsbns.contains(book.isbn()))
        .toList();

    log.info("중복 제거 후 남은 도서 수: {}", filtered.size());
    log.debug("남은 도서 ISBN: {}", filtered.stream().map(BookSaveRequestDto::isbn).toList());

    return filtered;

  }

}
