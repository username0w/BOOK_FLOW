package com.clover.bookflow.domain.auth.token.entity;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "token", nullable = false, unique = true, length = 512)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false, unique = true)
  private String jti;

  public RefreshToken(Member member, String token, Instant expiresAt, String jti) {
    this.member = member;
    this.token = token;
    this.expiresAt = expiresAt;
    this.jti = jti;
  }

  public static RefreshToken create(Member member, TokenWithMeta refreshToken) {
    return new RefreshToken(member, refreshToken.token(), refreshToken.expiresAt(),
        refreshToken.jti());
  }

  public void update(String token, Instant expiresAt, String jti) {
    this.token = token;
    this.expiresAt = expiresAt;
    this.jti = jti;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
