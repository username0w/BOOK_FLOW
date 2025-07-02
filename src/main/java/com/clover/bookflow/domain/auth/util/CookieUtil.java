package com.clover.bookflow.domain.auth.util;

import org.springframework.http.ResponseCookie;

public class CookieUtil {

  public static final String ACCESS_TOKEN = "accessToken";
  public static final String REFRESH_TOKEN = "refreshToken";

  // 쿠키 생성 - HttpOnly, Secure, SameSite 등 옵션 세팅
  // httpOnly 쿠키 사용 - JS 접근 차단 -> XSS 공격으로부터 보호
  // secure - HTTPS 에서만 쿠키 전송 ( 운영환경: true, 개발: false ) ->  MITM 공격 방어
  // sameSite - CSRF 공격 방어: Strict, Lax, None ( 일반: Strict / 외부 도메인 허용 필요시: None + secure )
  /*
   * "Strict": 완전한 제3자 요청 차단 (로그인 후 외부 링크 클릭 시 쿠키 안보냄)
   * "Lax": 일부 안전한 요청(GET 등)에만 쿠키 전송 허용
   * "None": 모든 요청에 쿠키 전송 (단, 이 경우 secure 필수)
   * */
  public static String createAccessTokenCookie(String token, int maxAgeSeconds) {
    return ResponseCookie.from(ACCESS_TOKEN, token)
        .httpOnly(false)
        .secure(false) // 운영환경에서는 true, 개발환경에서는 false로 세팅 권장
        .path("/")
        .maxAge(maxAgeSeconds)
        .sameSite("None")
        .build()
        .toString();
  }

  public static String createRefreshTokenCookie(String token, int maxAgeSeconds) {
    return ResponseCookie.from(REFRESH_TOKEN, token)
        .httpOnly(true)
        .secure(false) // 운영환경에서는 true, 개발환경에서는 false로 세팅 권장
        .path("/")
        .maxAge(maxAgeSeconds)
        .sameSite("None")
        .build()
        .toString();
  }

  // 쿠키 삭제용 빈 쿠키 생성
  public static String deleteTokenCookie(String cookieName) {
    return ResponseCookie.from(cookieName, "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build()
        .toString();
  }

}
