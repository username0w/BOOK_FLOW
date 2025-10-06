package com.clover.bookflow.domain.readbook.repository;

import com.clover.bookflow.domain.readbook.entity.ReadBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadBookRepository extends JpaRepository<ReadBook, Long> {

}
