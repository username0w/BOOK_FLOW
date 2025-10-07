package com.clover.bookflow.domain.book.service;

import com.clover.bookflow.domain.book.dto.BookAllSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookDetailSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookSearchResponse;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookDetail;
import com.clover.bookflow.domain.book.repository.BookDetailRepository;
import com.clover.bookflow.domain.book.repository.BookRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

  private final AladinApiService aladinApiService;

  private final BookRepository bookRepository;

  private final BookDetailRepository bookDetailRepository;
  // Todo : 상품 리스트 - 베스트셀러 조회

  // 책 crud

  // 도서 저장
  @Transactional
  public void saveBooksWithDetails(List<BookAllSaveRequestDto> books) {
    List<Book> entities = books.stream().map(BookAllSaveRequestDto::toEntity).toList();

    bookRepository.saveAll(entities);

    log.info("Saved {} books with details", entities.size());

  }

  @Transactional
  public void saveBasicBooks(Set<BookSaveRequestDto> books) {
    // 도서 저장
    List<Book> entities = books.stream()
        .map(BookSaveRequestDto::toEntity).toList();

    bookRepository.saveAll(entities);

    List<BookDetail> details = entities.stream()
        .map(BookDetail::new)
        .toList();

    bookDetailRepository.saveAll(details);
    log.info("Saved {} books", entities.size());
  }

  @Transactional
  public void saveBookDetails(List<BookDetailSaveRequestDto> docs) {
    // 비동기
    List<BookDetail> details = new ArrayList<>();
    for (BookDetailSaveRequestDto doc : docs) {
      Optional<Book> book = bookRepository.findByIsbn(doc.eaIsbn());
      if (book.isEmpty()) {
        continue;
      }
      Optional<BookDetail> existingDetailOpt = bookDetailRepository.findById(book.get().getId());

      BookDetail bookDetail;

      if (existingDetailOpt.isPresent()) {
        bookDetail = existingDetailOpt.get();
        bookDetail.updateDetail(
            doc.ddc(),
            doc.kdc(),
            doc.bookTbCntUrl(),
            doc.bookTbCnt(),
            doc.bookIntroductionUrl(),
            doc.bookIntroduction(),
            doc.bookSummaryUrl(),
            doc.bookSummary(),
            doc.subject(),
            doc.page()
        );
        bookDetail.changeStatus(doc.status());
      } else {
        bookDetail = doc.toEntity(book.get());
      }
      details.add(bookDetail);
    }
    bookDetailRepository.saveAll(details);
    log.info("Saved {} book details", details.size());
  }


  public Set<String> findExistingIsbns(Set<String> isbns) {
    if (isbns == null || isbns.isEmpty()) {
      return Collections.emptySet();
    }

    return bookRepository.findExistingIsbns(isbns);
  }


  public Book findOrCreateBookByIsbn(String isbn) {
    return null;
    //Todo: Book, BookDetail 구조 리팩토링 진행 및 전체 패키지 구조 리팩토링 진행
  }

  // 도서 검색
  // 읽은 도서 목록에 도서 추가 시 사용
  public Page<BookSearchResponse> searchBooks(String keyword, Pageable pageable) {
    return aladinApiService.searchBooks(keyword, pageable);
  }
}
