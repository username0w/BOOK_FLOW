package com.clover.bookflow.domain.book.dto;

import com.clover.bookflow.domain.book.entity.Book;
import com.clover.bookflow.domain.book.entity.BookDetail;
import java.time.LocalDate;

public record BookAllSaveRequestDto(
    String drCode,
    String drCodeName,
    String recomtitle,
    String recomauthor,
    String recompublisher,
    String recomisbn,
    String recomfilepath,
    String recommokcha,
    String recomcontens,
    String publishYear,
    String mokchFilePath

) {

  public Book toEntity() {
    LocalDate publishDate = LocalDate.parse(publishYear + "-01-01");
    Book book = new Book(recomisbn, recomtitle, recomauthor, recompublisher, recomfilepath,
        publishDate, null, drCodeName);
    BookDetail bookDetail = new BookDetail(book);
    bookDetail.updateDetail(null, drCode, mokchFilePath, recommokcha, null, null, null,
        recomcontens, drCode, null);
    book.changeBookDetail(bookDetail);
    return book;
  }


}
