package com.clover.bookflow.domain.book.repository;

import com.clover.bookflow.domain.book.entity.Book;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

  @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :isbns")
  Set<String> findExistingIsbns(@Param("isbns") Set<String> isbns);

  Optional<Book> findByIsbn(String isbn);
}
