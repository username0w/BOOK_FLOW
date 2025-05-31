package com.clover.bookflow.domain.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final Key key;
  private final long validityInMilliseconds;

  public JwtProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.expiration}") long validityInMilliseconds
  ) {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    this.validityInMilliseconds = validityInMilliseconds;
  }

  public String createToken(Authentication authentication) {
    String email = authentication.getName();

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .setSubject(email) // 토큰에 사용할 식별자 값 저장
        .setIssuedAt(now) // 발행 시간
        .setExpiration(validity) // 만료 시간
        .signWith(key) // 비밀키로 서명
        .compact(); // 토큰 생성
  }

  // JWT 토큰에서 사용자 정보(email) 추출
  public String getEmailFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims.getSubject(); // Subject는 이메일
  }

  // JWT 토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      parseClaims(token); // JWT를 파싱하여 유효성 검증
      return true; // 예외가 발생하지 않으면 유효한 토큰
    } catch (Exception e) {
      return false; // 예외 발생 시 유효하지 않은 토큰
    }
  }

  // JWT 토큰에서 Claims(페이로드) 추출
  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key) // 비밀키를 사용하여 서명 검증
        .build()
        .parseClaimsJws(token) // 토큰 파싱 및 서명 검증
        .getBody(); // Claims 객체 반환
  }
  /*
   * JWT = Header + Payload(Claims) + Signature
   * Claims : JWT 토큰의 사용자 정보, 만료시간 등
   * -> Payload 에 담기는 중요 정보
   * */

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    return (bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7)
        : null);
  }


}
