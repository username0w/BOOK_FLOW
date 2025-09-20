package com.clover.bookflow.domain.book.repository;

import com.clover.bookflow.domain.book.entity.BookDetail;
import com.clover.bookflow.domain.book.enums.BookDetailStatus;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookDetailRepository extends JpaRepository<BookDetail, Long> {

  @Query("SELECT b.book.isbn FROM BookDetail b WHERE b.book.isbn IN :isbns AND b.status = :status")
  Set<String> findByIsbnsAndStatus(@Param("isbns") Set<String> isbns,
      @Param("status") BookDetailStatus status);

}
