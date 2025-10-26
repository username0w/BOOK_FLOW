package com.clover.bookflow.domain.readbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookTestHelper;
import com.clover.bookflow.domain.book.service.BookService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.enums.MemberStatus;
import com.clover.bookflow.domain.member.enums.Role;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.request.UpdateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import com.clover.bookflow.domain.readbook.repository.ReadBookRepository;
import com.clover.bookflow.global.errorcode.ReadBookErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.ForbiddenException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.data.domain.Sort;

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

    private Member member;
    private Member otherMember;
    private Book book;

    @BeforeEach
    void setUp() {
        // 공통 테스트용 Member, 다른 Member
        member = MemberTestHelper.createTestMemberWithId(
                1L, UUID.randomUUID(), "test@example.com", "password123",
                "testNickname", Role.USER, MemberStatus.ACTIVE
        );

        otherMember = MemberTestHelper.createTestMemberWithId(
                2L, UUID.randomUUID(), "other@example.com", "password456",
                "otherNickname", Role.USER, MemberStatus.ACTIVE
        );

        // 공통 테스트용 Book
        book = BookTestHelper.createBook();
    }

    @Nested
    @DisplayName("addReadBook 메서드 테스트")
    class AddReadBookTests {

        @Test
        @DisplayName("도서가 DB에 없는 경우, 도서를 생성하고 읽은 책 등록에 성공한다")
        void shouldCreateBookAndSaveReadBook_whenBookNotExist() {
            Long memberId = member.getId();
            CreateReadBookRequest request = new CreateReadBookRequest("1234567890", LocalDate.now());

            given(memberService.findMemberById(memberId)).willReturn(member);
            given(bookService.findOrCreateBookByIsbn(request.isbn())).willReturn(book);
            given(readBookRepository.save(any(ReadBook.class))).willAnswer(i -> i.getArgument(0));

            ReadBookResponse response = readBookService.addReadBook(request, memberId);

            assertThat(response).isNotNull();
            verify(bookService).findOrCreateBookByIsbn(request.isbn());
            verify(readBookRepository).save(any(ReadBook.class));
        }

        @Test
        @DisplayName("도서가 DB에 이미 존재하는 경우, 읽은 책 등록에 성공한다")
        void shouldSaveReadBook_whenBookExists() {
            Long memberId = member.getId();
            CreateReadBookRequest request = new CreateReadBookRequest("1234567890", LocalDate.now());

            given(memberService.findMemberById(memberId)).willReturn(member);
            given(bookService.findOrCreateBookByIsbn(request.isbn())).willReturn(book);
            given(readBookRepository.save(any(ReadBook.class))).willAnswer(i -> i.getArgument(0));

            ReadBookResponse response = readBookService.addReadBook(request, memberId);

            assertThat(response).isNotNull();
            verify(bookService).findOrCreateBookByIsbn(request.isbn());
            verify(readBookRepository).save(any(ReadBook.class));
        }
    }

    @Nested
    @DisplayName("updateReadBook 메서드 테스트")
    class UpdateReadBookTests {

        @Test
        @DisplayName("자신의 읽은 책이면 업데이트 성공")
        void shouldUpdateReadBook_whenMemberIsOwner() {
            Long readBookId = 100L;
            LocalDate newDate = LocalDate.now();
            UpdateReadBookRequest request = new UpdateReadBookRequest(newDate);

            ReadBook readBook = ReadBook.create(member, book, LocalDate.now());
            given(readBookRepository.findById(readBookId)).willReturn(Optional.of(readBook));

            ReadBookResponse response = readBookService.updateReadBook(readBookId, request, member.getId());

            assertThat(response).isNotNull();
            assertThat(readBook.getReadDate()).isEqualTo(newDate);
        }

        @Test
        @DisplayName("다른 사람 읽은 책이면 ForbiddenException 발생")
        void shouldThrowForbiddenException_whenNotOwner() {
            Long readBookId = 100L;
            UpdateReadBookRequest request = new UpdateReadBookRequest(LocalDate.now());

            ReadBook readBook = ReadBook.create(member, book, LocalDate.now());
            given(readBookRepository.findById(readBookId)).willReturn(Optional.of(readBook));

            assertThatThrownBy(() -> readBookService.updateReadBook(readBookId, request, otherMember.getId()))
                    .isInstanceOf(ForbiddenException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReadBookErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("읽은 책이 없으면 BusinessException 발생")
        void shouldThrowBusinessException_whenReadBookNotFound() {
            given(readBookRepository.findById(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> readBookService.updateReadBook(
                    100L, new UpdateReadBookRequest(LocalDate.now()), member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReadBookErrorCode.READ_BOOK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteReadBook 메서드 테스트")
    class DeleteReadBookTests {

        @Test
        @DisplayName("자신의 읽은 책이면 삭제 성공")
        void shouldDeleteReadBook_whenMemberIsOwner() {
            Long readBookId = 100L;

            ReadBook readBook = ReadBook.create(member, book, LocalDate.now());
            given(readBookRepository.findById(readBookId)).willReturn(Optional.of(readBook));

            readBookService.deleteReadBook(readBookId, member.getId());

            verify(readBookRepository).delete(readBook);
        }

        @Test
        @DisplayName("다른 사람 읽은 책이면 ForbiddenException 발생")
        void shouldThrowForbiddenException_whenNotOwner() {
            Long readBookId = 100L;

            ReadBook readBook = ReadBook.create(member, book, LocalDate.now());
            given(readBookRepository.findById(readBookId)).willReturn(Optional.of(readBook));

            assertThatThrownBy(() -> readBookService.deleteReadBook(readBookId, otherMember.getId()))
                    .isInstanceOf(ForbiddenException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReadBookErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("읽은 책이 없으면 BusinessException 발생")
        void shouldThrowBusinessException_whenReadBookNotFound() {
            given(readBookRepository.findById(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> readBookService.deleteReadBook(100L, member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReadBookErrorCode.READ_BOOK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getReadBooksByMember 메서드 테스트")
    class GetReadBooksByMemberTests {

        @Test
        @DisplayName("회원의 읽은 책 목록 조회 성공")
        void shouldReturnReadBooks_whenMemberHasReadBooks() {
            ReadBook rb1 = ReadBook.create(member, book, LocalDate.now());
            ReadBook rb2 = ReadBook.create(member, book, LocalDate.now().minusDays(1));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());
            given(readBookRepository.findAllByMemberId(member.getId(), pageable))
                    .willReturn(new PageImpl<>(List.of(rb1, rb2)));

            Page<ReadBookResponse> responses = readBookService.getReadBooksByMember(member.getId(), pageable);

            assertThat(responses.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("읽은 책이 없으면 빈 페이지 반환")
        void shouldReturnEmptyList_whenNoReadBooks() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());
            given(readBookRepository.findAllByMemberId(member.getId(), pageable))
                    .willReturn(Page.empty());

            Page<ReadBookResponse> responses = readBookService.getReadBooksByMember(member.getId(), pageable);

            assertThat(responses.getContent()).isEmpty();
        }
    }

}

