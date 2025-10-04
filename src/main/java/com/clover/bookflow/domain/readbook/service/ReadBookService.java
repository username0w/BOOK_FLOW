package com.clover.bookflow.domain.readbook.service;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.service.BookService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import com.clover.bookflow.domain.readbook.repository.ReadBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadBookService {

  private final ReadBookRepository readBookRepository;
  private final MemberService memberService;
  private final BookService bookService;

  @Transactional
  public ReadBookResponse addReadBook(CreateReadBookRequest request, Long memberId) {
    // 1. 회원 조회
    Member member = memberService.findMemberById(memberId);

    // 2. 책 조회 (DB에 없으면 API 호출 후 저장)
    Book book = bookService.findOrCreateBookByIsbn(request.isbn());

    // 3. 읽은 책 엔티티 생성 (중복 체크는 ReadBookRepository에서 별도 처리하거나 unique 제약조건 활용)
    ReadBook readBook = ReadBook.create(member, book, request.readDate());

    // 4. 저장
    readBookRepository.save(readBook);

    // 5. DTO 변환 후 반환
    return ReadBookResponse.from(readBook);
  }

}
