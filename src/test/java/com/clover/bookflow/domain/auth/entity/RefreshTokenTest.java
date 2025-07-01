package com.clover.bookflow.domain.auth.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.member.entity.Member;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefreshTokenTest {

  @DisplayName("RefreshToken 객체가 생성된다")
  @Test
  void createRefreshToken() {
    // given
//    Long memberId = 1L;
    Member member = Member.create("test@example.com", "password123", "길똥이");
    String token = "sample-refresh-token";
    String jti = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

    TokenWithMeta tokenWithMeta = new TokenWithMeta(token, jti, expiresAt);

    // when
    RefreshToken refreshToken = RefreshToken.create(member, tokenWithMeta);

    // then
    assertThat(refreshToken).isNotNull();
    assertThat(refreshToken.getMember()).isEqualTo(member);
    assertThat(refreshToken.getToken()).isEqualTo(token);
    assertThat(refreshToken.getExpiresAt()).isAfter(Instant.now());
    assertThat(refreshToken.getJti()).isNotNull();

  }

  @DisplayName("만료된 RefreshToken 은 expired 상태로 인식된다")
  @Test
  void shouldReturnTrue_whenTokenIsExpired() {
    // given
    Member member = Member.create("test@example.com", "password123", "길똥이");
    String token = "sample-refresh-token";
    String jti = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().minus(1, ChronoUnit.DAYS);

    TokenWithMeta tokenWithMeta = new TokenWithMeta(token, jti, expiresAt);

    // when
    RefreshToken expiredToken = RefreshToken.create(member, tokenWithMeta);

    assertThat(expiredToken.isExpired()).isTrue();

  }

}
