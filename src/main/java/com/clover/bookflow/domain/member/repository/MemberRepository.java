package com.clover.bookflow.domain.member.repository;

import com.clover.bookflow.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Optional<Member> findByEmail(String email);
}
