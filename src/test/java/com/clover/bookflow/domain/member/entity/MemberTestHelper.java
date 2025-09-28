package com.clover.bookflow.domain.member.entity;

import com.clover.bookflow.domain.member.enums.MemberStatus;
import com.clover.bookflow.domain.member.enums.Role;
import java.util.UUID;

public class MemberTestHelper {

  private static final String DEFAULT_EMAIL = "test@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String DEFAULT_NICKNAME = "testNickname";

  private static final String ADMIN_EMAIL = "admin@example.com";
  private static final String ADMIN_PASSWORD = "password456";
  private static final String ADMIN_NICKNAME = "testAdmin";

//  public static Member createTestUser() {
//    return new Member(DEFAULT_EMAIL, DEFAULT_PASSWORD,
//        DEFAULT_NICKNAME);
//  }

  public static Member createTestUser() {
    UUID testUuid = UUID.randomUUID();
    return new Member(testUuid, DEFAULT_EMAIL, DEFAULT_PASSWORD,
        DEFAULT_NICKNAME, Role.USER, MemberStatus.ACTIVE);
  }

  public static Member createTestUser(UUID uuid, String email, String password, String nickname,
      Role role, MemberStatus status) {
    return new Member(uuid, email, password, nickname, role, status);
  }

  public static Member createTestMemberWithId(
      Long id,
      UUID uuid,
      String email,
      String password,
      String nickname,
      Role role,
      MemberStatus status
  ) {
    return new Member(id, uuid, email, password, nickname, role, status);
  }

  public static Member createTestOtherUser() {
    UUID testUuid = UUID.randomUUID();
    return new Member(testUuid, "other@example.com", "otherpassword&", "otherNickname", Role.USER,
        MemberStatus.ACTIVE);
  }


  public static Member createTestAdmin() {
    UUID testUuid = UUID.randomUUID();
    return new Member(testUuid, ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_NICKNAME, Role.ADMIN,
        MemberStatus.ACTIVE);
  }


}
