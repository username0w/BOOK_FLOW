package com.clover.bookflow.domain.member.repository;

import com.clover.bookflow.domain.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Optional<Member> findByEmail(String email);

  @Query("SELECT m FROM Member m WHERE m.uuid = :uuid")
  Optional<Member> findByUuid(@Param("uuid") UUID uuid);
}
