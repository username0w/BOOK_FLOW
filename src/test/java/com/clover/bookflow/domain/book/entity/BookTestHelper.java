package com.clover.bookflow.domain.book.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 테스트에서 사용할 Book 관련 도우미 클래스. - 실제 Book 객체 생성 (도메인 테스트, 통합 테스트) - mock Book 객체 생성 (단위 테스트용 stub)
 */
public class BookTestHelper {

  // 실제 객체 생성 (new Book(...))

  public static Book createBook() {
    return new Book(
        "978-89-01-12345-6",
        "테스트 책 제목",
        "홍길동",
        "테스트출판사",
        "https://example.com/cover.jpg",
        LocalDate.of(2020, 1, 1),
        "이 책은 테스트용입니다.",
        "프로그래밍"
    );
  }

  public static Book createBookWithDetail() {
    Book book = createBook();
    BookDetail bookDetail = new BookDetail(book);
    book.changeBookDetail(bookDetail);  // 양방향 연관관계 설정
    return book;
  }

  public static BookDetail createBookDetail(Book book) {
    BookDetail bookDetail = new BookDetail(book);
    book.changeBookDetail(bookDetail); // 양방향 연관관계 설정
    return bookDetail;
  }

  public static BookDetail createFullBookDetail(Book book) {
    BookDetail bookDetail = createBookDetail(book);

    bookDetail.updateDetail(
        "100", // ddc
        "200", // kdc
        "https://example.com/toc",
        "목차 내용입니다",
        "https://example.com/intro",
        "책 소개입니다",
        "https://example.com/summary",
        "요약입니다",
        "프로그래밍",
        "350"
    );

    return bookDetail;
  }

  public static Book createBookWithFullDetail() {
    Book book = createBook();
    BookDetail fullDetail = createFullBookDetail(book);
    book.changeBookDetail(fullDetail);
    return book;
  }

  // Mockito mock 객체 생성

  public static Book mockBook(Long id) {
    Book book = mock(Book.class);
    when(book.getId()).thenReturn(id);
    when(book.getTitle()).thenReturn("Mock 책 " + id);
    when(book.getAuthor()).thenReturn("Mock 저자");
    // 필요한 메서드 추가 stub 가능
    return book;
  }

  public static List<Book> mockBooks(Long... ids) {
    return Arrays.stream(ids)
        .map(BookTestHelper::mockBook)
        .collect(Collectors.toList());
  }
}
