package com.clover.bookflow.domain.member.entity;

import java.util.UUID;

public class MemberTestHelper {

  private static final String DEFAULT_EMAIL = "test@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String DEFAULT_NICKNAME = "testNickname";

//  public static Member createTestUser() {
//    return new Member(DEFAULT_EMAIL, DEFAULT_PASSWORD,
//        DEFAULT_NICKNAME);
//  }

  public static Member createTestUser() {
    UUID testUuid = UUID.randomUUID();
    return new Member(testUuid, DEFAULT_EMAIL, DEFAULT_PASSWORD,
        DEFAULT_NICKNAME);
  }


}
