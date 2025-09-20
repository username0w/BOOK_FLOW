package com.clover.bookflow.domain.book.dto;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookDetail;
import com.clover.bookflow.domain.book.enums.BookDetailStatus;

public record BookDetailSaveRequestDto(
    String eaIsbn,
    String ddc,
    String kdc,
    String bookTbCntUrl,
    String bookTbCnt,
    String bookIntroductionUrl,
    String bookIntroduction,
    String bookSummaryUrl,
    String bookSummary,
    String subject,
    String page,
    BookDetailStatus status
) {

  public static BookDetailSaveRequestDto noDetail(String isbn) {
    return new BookDetailSaveRequestDto(isbn, null, null, null, null, null, null, null, null, null,
        null, BookDetailStatus.NO_DETAIL);
  }

  public static BookDetailSaveRequestDto error(String isbn) {
    return new BookDetailSaveRequestDto(isbn, null, null, null, null, null, null, null, null, null,
        null, BookDetailStatus.ERROR);
  }

  public static BookDetailSaveRequestDto complete(String isbn, String ddc, String kdc,
      String bookTbCntUrl, String bookTbCnt, String bookIntroductionUrl, String bookIntroduction,
      String bookSummaryUrl, String bookSummary, String subject, String page) {
    return new BookDetailSaveRequestDto(isbn, ddc, kdc, bookTbCntUrl, bookTbCnt,
        bookIntroductionUrl, bookIntroduction, bookSummaryUrl, bookSummary, subject, page,
        BookDetailStatus.COMPLETE);
  }

  public BookDetail toEntity(Book book) {
    BookDetail bookDetail = new BookDetail(book);
    bookDetail.updateDetail(ddc, kdc, bookTbCntUrl, bookTbCnt, bookIntroductionUrl,
        bookIntroduction, bookSummaryUrl, bookSummary, subject, page);
    bookDetail.changeStatus(status);
    return bookDetail;
  }


}
