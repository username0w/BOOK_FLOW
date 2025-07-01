package com.clover.bookflow.domain.auth.security;

import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.global.errorcode.TokenErrorCode;
import com.clover.bookflow.global.exception.auth.InvalidTokenException;
import com.clover.bookflow.global.exception.auth.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final String secretKey;
  private final long accessTokenValidityInMs;
  private final long refreshTokenValidityInMs;
  private Key key;

  public JwtProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.access-token-expiration-ms}") long accessTokenValidityInMs,
      @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenValidityInMs
  ) {
    this.secretKey = secretKey;
    this.accessTokenValidityInMs = accessTokenValidityInMs;
    this.refreshTokenValidityInMs = refreshTokenValidityInMs;
  }

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public TokenWithMeta createAccessToken(String username, List<String> roles) {
    return createToken(username, roles, accessTokenValidityInMs);
  }

  public TokenWithMeta createRefreshToken(String username, List<String> roles) {
    return createToken(username, roles, refreshTokenValidityInMs);
  }

  private TokenWithMeta createToken(String email, List<String> roles, long validityInMs) {
    String jti = UUID.randomUUID().toString();
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMs);

    Claims claims = Jwts.claims().setSubject(email);
    claims.put("roles", roles);

    String token = Jwts.builder()
        .setSubject(email) // 토큰에 사용할 식별자 값 저장
        .setId(jti)
        .setIssuedAt(now) // 발행 시간
        .setExpiration(validity) // 만료 시간
        .signWith(key, SignatureAlgorithm.HS256) // 비밀키로 서명
        .compact(); // 토큰 생성

    Instant expiresAt = validity.toInstant();

    return TokenWithMeta.of(token, jti, expiresAt);
  }

  // JWT 토큰에서 사용자 정보(email) 추출
  public String getEmailFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims.getSubject(); // Subject는 이메일
  }

  public String getJtiFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims.getId();
  }

  public Instant getExpiresAtFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims.getExpiration().toInstant();
  }

  // JWT 토큰 유효성 검증
  public void validateToken(String token) {
    parseClaims(token);
//    // 예외가 발생하면 여기서 던지고,
//    // 호출하는 쪽(필터 등)에서 예외 처리하도록 책임을 위임
    // 여기서 예외 처리하지 않고 Controller 에 도달하기 전에 필터에서 처리하는 방식으로 변경
    // 파싱에서 예외 발생하면 바로 던짐
  }

  // JWT 토큰에서 Claims(페이로드) 추출
  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(key) // 비밀키를 사용하여 서명 검증
          .build()
          .parseClaimsJws(token) // 토큰 파싱 및 서명 검증
          .getBody(); // Claims 객체 반환
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException();
    } catch (JwtException e) {
      throw new InvalidTokenException(TokenErrorCode.INVALID_TOKEN);
    }

  }
  /*
   * JWT = Header + Payload(Claims) + Signature
   * Claims : JWT 토큰의 사용자 정보, 만료시간 등
   * -> Payload 에 담기는 중요 정보
   * */


}
