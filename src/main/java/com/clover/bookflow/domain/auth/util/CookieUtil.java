package com.clover.bookflow.domain.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

  public static String extractFromCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(cookieName)) {
        return cookie.getValue();
      }
    }
    return null;
  }

}
