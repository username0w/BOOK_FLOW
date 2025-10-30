package com.clover.bookflow.domain.readbook.service;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.service.BookService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.domain.readbook.dto.request.CreateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.request.UpdateReadBookRequest;
import com.clover.bookflow.domain.readbook.dto.response.ReadBookResponse;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import com.clover.bookflow.domain.readbook.repository.ReadBookRepository;
import com.clover.bookflow.global.errorcode.ReadBookErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // 등록일로 처리
        // 지금 이건 단순한 읽은 읽고있는~ 읽었던 포함하는 도서 목록

        // 4. 저장
        readBookRepository.save(readBook);

        // 5. DTO 변환 후 반환
        return ReadBookResponse.from(readBook);
    }

    @Transactional
    public ReadBookResponse updateReadBook(Long readBookId, UpdateReadBookRequest request, Long memberId) {
        ReadBook readBook = readBookRepository.findById(readBookId)
                .orElseThrow(() -> new BusinessException(ReadBookErrorCode.READ_BOOK_NOT_FOUND));

        if (!readBook.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(ReadBookErrorCode.ACCESS_DENIED);
        }

        readBook.updateReadDate(request.readDate());
        return ReadBookResponse.from(readBook);
    }

    @Transactional
    public void deleteReadBook(Long readBookId, Long memberId) {
        ReadBook readBook = readBookRepository.findById(readBookId)
                .orElseThrow(() -> new BusinessException(ReadBookErrorCode.READ_BOOK_NOT_FOUND));

        if (!readBook.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(ReadBookErrorCode.ACCESS_DENIED);
        }

        readBookRepository.delete(readBook);
    }

    @Transactional(readOnly = true)
    public Page<ReadBookResponse> getReadBooksByMember(Long memberId, Pageable pageable) {
        Page<ReadBook> readBooks = readBookRepository.findAllByMemberId(memberId, pageable);

        return readBooks.map(ReadBookResponse::from);
    }

    /**
     * SecurityContext에서 Member ID 가져온 후 DB에서 실제 Member 확인
     */
    private Member getCurrentMemberFromDb() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = ((CustomMemberDetails) auth.getPrincipal()).getId();
        return memberService.findMemberById(memberId); // DB 조회 후 없으면 예외 발생
    }

}
