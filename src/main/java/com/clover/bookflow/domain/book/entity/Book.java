package com.clover.bookflow.domain.book.entity;

import com.clover.bookflow.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "books")
public class Book extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String isbn;

  private String title;

  private String author;

  private String publisher;

  private String coverImgUrl;

  private LocalDate publishedDate;

  @Lob
  private String shortDescription;

  private String category;


  @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private BookDetail bookDetail;

  public Book(String isbn, String title, String author, String publisher, String coverImgUrl,
      LocalDate publishedDate, String shortDescription, String category) {
    this.isbn = isbn;
    this.title = title;
    this.author = author;
    this.publisher = publisher;
    this.coverImgUrl = coverImgUrl;
    this.publishedDate = publishedDate;
    this.shortDescription = shortDescription;
    this.category = category;
  }

  public void changeBookDetail(BookDetail bookDetail) {
    this.bookDetail = bookDetail;
    if (bookDetail.getBook() != this) {
      bookDetail.changeBook(this);
    }
  }

}


