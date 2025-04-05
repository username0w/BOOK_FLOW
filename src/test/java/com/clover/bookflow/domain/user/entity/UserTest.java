package com.clover.bookflow.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class UserTest {

  @Test
  @DisplayName("User 생성 테스트")
  void createUser() {
    User user = User.create("hkd111@example.com", "password123", "길똥이");

    assertThat(user.getEmail()).isEqualTo("hkd111@example.com");
    assertThat(user.getNickname()).isEqualTo("길똥이");
  }

}
