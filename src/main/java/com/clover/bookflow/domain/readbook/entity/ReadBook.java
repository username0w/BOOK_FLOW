package com.clover.bookflow.domain.readbook.entity;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "read_books")
public class ReadBook extends BaseTimeEntity { // 사용자가 읽은 도서 목록 관리, 중간 엔티티

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id")
  private Book book;

  private LocalDate readDate;

  private ReadBook(Member member, Book book, LocalDate readDate) {
    this.member = member;
    this.book = book;
    this.readDate = readDate;
  }

  public static ReadBook create(Member member, Book book, LocalDate readDate) {
    return new ReadBook(member, book, readDate);
  }

}
