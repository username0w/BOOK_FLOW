package com.clover.bookflow.domain.member.entity;

import com.clover.bookflow.domain.member.enums.MemberStatus;
import com.clover.bookflow.domain.member.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
  private UUID uuid;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus memberStatus;

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
  }

  public Member(String email, String password, String nickname) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.role = Role.USER;
    this.memberStatus = MemberStatus.ACTIVE;
  }


  protected Member(UUID uuid, String email, String password, String nickname) {
    this.uuid = uuid;
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.role = Role.USER;
    this.memberStatus = MemberStatus.ACTIVE;
  }


  public static Member create(String email, String password, String nickname) {
    return new Member(email, password, nickname);
  }

  public void withdraw() {
    this.memberStatus = MemberStatus.DELETED;
  }

}
