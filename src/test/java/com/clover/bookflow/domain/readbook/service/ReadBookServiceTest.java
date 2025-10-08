package com.clover.bookflow.domain.readbook.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookTestHelper;
import com.clover.bookflow.domain.book.service.BookService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import com.clover.bookflow.domain.readbook.repository.ReadBookRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadBookServiceTest {

  @Mock
  private MemberService memberService;

  @Mock
  private BookService bookService;

  @Mock
  private ReadBookRepository readBookRepository;

  @InjectMocks
  private ReadBookService readBookService;

  @Nested
  @DisplayName("addReadBook 메서드 테스트")
  class AddReadBookTests {

    @DisplayName("도서가 DB에 없는 경우, 도서를 생성하고 읽은 책 등록에 성공한다")
    @Test
    void shouldCreateBookAndSaveReadBook_whenBookNotExist() {
      // given
      Long memberId = 1L;
      CreateReadBookRequest request = new CreateReadBookRequest(
          "1234567890",    // isbn
          LocalDate.now()
      );

      Member member = MemberTestHelper.createTestUser();
      Book newBook = BookTestHelper.createBook();

      given(memberService.findMemberById(memberId)).willReturn(member);
      given(bookService.findOrCreateBookByIsbn(request.isbn())).willReturn(newBook);
      given(readBookRepository.save(any(ReadBook.class))).willAnswer(
          invocation -> invocation.getArgument(0));

      // when
      ReadBookResponse response = readBookService.addReadBook(request, memberId);

      // then
      assertNotNull(response);
      verify(bookService).findOrCreateBookByIsbn(request.isbn());
      verify(readBookRepository).save(any(ReadBook.class));
    }

    @DisplayName("도서가 DB에 이미 존재하는 경우, 읽은 책 등록에 성공한다")
    @Test
    void shouldSaveReadBook_whenBookExists() {
      // given
      Long memberId = 1L;
      CreateReadBookRequest request = new CreateReadBookRequest(
          "1234567890",    // isbn
          LocalDate.now()
      );

      Member member = MemberTestHelper.createTestUser();
      Book existingBook = BookTestHelper.createBook();  // 이미 DB에 있다고 가정

      given(memberService.findMemberById(memberId)).willReturn(member);
      given(bookService.findOrCreateBookByIsbn(request.isbn())).willReturn(existingBook);
      given(readBookRepository.save(any(ReadBook.class))).willAnswer(
          invocation -> invocation.getArgument(0));

      // when
      ReadBookResponse response = readBookService.addReadBook(request, memberId);

      // then
      assertNotNull(response);
      verify(bookService).findOrCreateBookByIsbn(request.isbn());
      verify(readBookRepository).save(any(ReadBook.class));
    }
  }
}
