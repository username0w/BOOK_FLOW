package com.clover.bookflow.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.book.dto.BookAllSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookDetailSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookSaveRequestDto;
import com.clover.bookflow.domain.book.dto.BookSearchResponse;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookDetail;
import com.clover.bookflow.domain.book.repository.BookDetailRepository;
import com.clover.bookflow.domain.book.repository.BookRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("BookService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @InjectMocks
  private BookService bookService;

  @Mock
  private AladinApiService aladinApiService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookDetailRepository bookDetailRepository;

  private Book book;
  private BookDetail detail;

  @BeforeEach
  void setUp() {
    book = new Book("isbn123", "title", "author", "publisher",
        "cover", LocalDate.now(), "description", "category");
    detail = new BookDetail(book);
  }

  @Nested
  @DisplayName("saveBooksWithDetails()")
  class SaveBooksWithDetailsTest {

    @DisplayName("도서 리스트를 저장한다")
    @Test
    void saveBooksWithDetails_shouldSaveAllBooks() {
      BookAllSaveRequestDto dto = mock(BookAllSaveRequestDto.class);
      given(dto.toEntity()).willReturn(book);

      bookService.saveBooksWithDetails(List.of(dto));

      verify(bookRepository).saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("saveBasicBooks()")
  class SaveBasicBooksTest {

    @DisplayName("Book과 BookDetail을 함께 저장한다")
    @Test
    void saveBasicBooks_shouldSaveBooksAndDetails() {
      BookSaveRequestDto dto = mock(BookSaveRequestDto.class);
      given(dto.toEntity()).willReturn(book);

      bookService.saveBasicBooks(Set.of(dto));

      verify(bookRepository).saveAll(anyList());
      verify(bookDetailRepository).saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("saveBookDetails()")
  class SaveBookDetailsTest {

    @DisplayName("BookDetail이 없을 경우 새로 저장한다")
    @Test
    void saveBookDetails_shouldSaveNewBookDetails() {
      BookDetailSaveRequestDto dto = mock(BookDetailSaveRequestDto.class);
      given(dto.eaIsbn()).willReturn("isbn123");
      given(bookRepository.findByIsbn("isbn123")).willReturn(Optional.of(book));
      given(bookDetailRepository.findById(any())).willReturn(Optional.empty());
      given(dto.toEntity(book)).willReturn(detail);

      bookService.saveBookDetails(List.of(dto));

      verify(bookDetailRepository).saveAll(anyList());
    }

    @DisplayName("기존 BookDetail이 있으면 업데이트한다")
    @Test
    void saveBookDetails_shouldUpdateExistingBookDetails() {
      BookDetailSaveRequestDto dto = mock(BookDetailSaveRequestDto.class);
      given(dto.eaIsbn()).willReturn("isbn123");
      given(bookRepository.findByIsbn("isbn123")).willReturn(Optional.of(book));
      given(bookDetailRepository.findById(any())).willReturn(Optional.of(detail));

      bookService.saveBookDetails(List.of(dto));

      verify(bookDetailRepository).saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("findExistingIsbns()")
  class FindExistingIsbnsTest {

    @DisplayName("입력값이 null이면 빈 Set을 반환한다")
    @Test
    void returnEmptySetIfNull() {
      Set<String> result = bookService.findExistingIsbns(null);
      assertThat(result).isEmpty();
    }

    @DisplayName("입력값이 빈 Set이면 빈 Set을 반환한다")
    @Test
    void returnEmptySetIfEmpty() {
      Set<String> result = bookService.findExistingIsbns(Set.of());
      assertThat(result).isEmpty();
    }

    @DisplayName("DB에 존재하는 ISBN만 반환한다")
    @Test
    void returnMatchingIsbns() {
      Set<String> input = Set.of("isbn1", "isbn2");
      Set<String> output = Set.of("isbn2");

      given(bookRepository.findExistingIsbns(input)).willReturn(output);

      Set<String> result = bookService.findExistingIsbns(input);

      assertThat(result).isEqualTo(output);
    }
  }

  @Nested
  @DisplayName("findOrCreateBookByIsbn()")
  class FindOrCreateBookByIsbnTest {

    @DisplayName("이미 존재하는 도서는 그대로 반환한다")
    @Test
    void returnExistingBook() {
      given(bookRepository.findByIsbn("isbn123")).willReturn(Optional.of(book));

      Book result = bookService.findOrCreateBookByIsbn("isbn123");

      assertThat(result).isEqualTo(book);
      verify(aladinApiService, never()).fetchBookByIsbn(any());
    }

    @DisplayName("존재하지 않으면 API로 가져와 저장 후 반환한다")
    @Test
    void fetchAndSaveIfNotExists() {
      given(bookRepository.findByIsbn("isbn123")).willReturn(Optional.empty());

      BookSaveRequestDto dto = mock(BookSaveRequestDto.class);
      given(aladinApiService.fetchBookByIsbn("isbn123")).willReturn(dto);
      given(dto.toEntity()).willReturn(book);
      given(bookRepository.save(book)).willReturn(book);

      Book result = bookService.findOrCreateBookByIsbn("isbn123");

      assertThat(result).isEqualTo(book);
      verify(bookRepository).save(book);
    }
  }

  @Nested
  @DisplayName("searchBooks()")
  class SearchBooksTest {

    @DisplayName("알라딘 API에 위임하여 도서를 검색한다")
    @Test
    void delegateSearchToApi() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<BookSearchResponse> mockPage = new PageImpl<>(List.of());

      given(aladinApiService.searchBooks("test", pageable)).willReturn(mockPage);

      Page<BookSearchResponse> result = bookService.searchBooks("test", pageable);

      assertThat(result).isEqualTo(mockPage);
    }
  }
}
