package com.clover.bookflow.domain.readbook.repository;

import com.clover.bookflow.domain.readbook.entity.ReadBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadBookRepository extends JpaRepository<ReadBook, Long> {

    Page<ReadBook> findAllByMemberId(Long memberId, Pageable pageable);
}
