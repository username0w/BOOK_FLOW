package com.clover.bookflow.domain.book.entity;

import com.clover.bookflow.domain.book.enums.BookDetailStatus;
import com.clover.bookflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book_detail")
public class BookDetail extends BaseTimeEntity {

  @Id
  private Long id;

  @MapsId
  @OneToOne
  @JoinColumn(name = "book_id")
  private Book book;

  private String ddc;

  private String kdc;

  private String bookTbCntUrl; // 목차 URL

  @Lob
  private String bookTbCnt;

  private String bookIntroductionUrl; // 책소개 URL

  @Lob
  private String bookIntroduction;

  private String bookSummaryUrl; // 책요약 URL

  @Lob
  private String bookSummary;

  private String subject;

  private String pageCount;

  // 적재 진행 상태 status Book 저장 시 기본값 적재 전.
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookDetailStatus status;

  private Integer retryCount;

  private String lastErrorMessage;

  private LocalDateTime lastTriedAt;

  public BookDetail(Book book) {
    this.book = book;
    this.status = BookDetailStatus.READY;
    this.retryCount = 0;
  }

  public void changeBook(Book book) {
    this.book = book;
    if (book.getBookDetail() != this) {
      book.changeBookDetail(this);
    }
  }

  public void updateDetail(String ddc, String kdc, String bookTbCntUrl, String bookTbCnt,
      String bookIntroductionUrl, String bookIntroduction, String bookSummaryUrl,
      String bookSummary, String subject, String pageCount) {
    this.ddc = ddc;
    this.kdc = kdc;
    this.bookTbCntUrl = bookTbCntUrl;
    this.bookTbCnt = bookTbCnt;
    this.bookIntroductionUrl = bookIntroductionUrl;
    this.bookIntroduction = bookIntroduction;
    this.bookSummaryUrl = bookSummaryUrl;
    this.bookSummary = bookSummary;
    this.subject = subject;
    this.pageCount = pageCount;
  }

  public void changeStatus(BookDetailStatus status) {
    this.status = status;
  }

  public void incrementRetryCount() {
    if (this.retryCount == null) {
      this.retryCount = 0;
    }
    this.retryCount++;
  }


}
