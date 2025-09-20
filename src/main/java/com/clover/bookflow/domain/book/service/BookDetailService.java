package com.clover.bookflow.domain.book.service;

import com.clover.bookflow.domain.book.enums.BookDetailStatus;
import com.clover.bookflow.domain.book.repository.BookDetailRepository;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookDetailService {

  private final BookDetailRepository bookDetailRepository;

  public Set<String> findCompletedIsbns(Set<String> isbns) {
    if (isbns == null || isbns.isEmpty()) {
      return Collections.emptySet();
    }
    return bookDetailRepository.findByIsbnsAndStatus(isbns, BookDetailStatus.COMPLETE);
  }

}
