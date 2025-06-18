package com.clover.bookflow.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class MemberTest {

  @Test
  @DisplayName("Member 생성 테스트")
  void createMember() {
    Member member = Member.create("hkd111@example.com", "password123", "길똥이");

    assertThat(member.getEmail()).isEqualTo("hkd111@example.com");
    assertThat(member.getNickname()).isEqualTo("길똥이");
  }

}
