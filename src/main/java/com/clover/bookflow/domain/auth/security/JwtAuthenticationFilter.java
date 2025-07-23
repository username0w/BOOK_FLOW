package com.clover.bookflow.domain.auth.security;

import com.clover.bookflow.domain.auth.service.CustomMemberDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final CustomMemberDetailsService customMemberDetailsService;

  private static final List<String> WHITELIST = List.of(
      "/auth/signup",
      "/auth/login",
      "/auth/refresh",
      "/auth/logout",
      "/swagger-ui",
      "/v3/api-docs",
      "/docs"
  );

  // 요청마다 실행됨: JWT 검증 및 사용자 인증 처리
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();

    if (isWhitelisted(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = resolveToken(request); // 헤더에서 토큰 추출

    if (token != null) {
      try {
        jwtProvider.validateToken(token); // 토큰 유효성 검증
        UUID uuid = jwtProvider.getUuidFromToken(token); // 토큰에서 이메일 추출
        UserDetails userDetails = customMemberDetailsService.loadUserByUuid(uuid); // uuid 로 사용자 조회

        // 인증 객체 생성 및 설정
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 저장
      } catch (ExpiredJwtException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("토큰이 만료되었습니다.");
        return;

      } catch (JwtException | UsernameNotFoundException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("유효하지 않은 토큰입니다.");
        return;
      }
    }

    filterChain.doFilter(request, response); // 다음 필터로 넘김
  }

  private boolean isWhitelisted(String path) {
    String pathWithoutVersion = path.replaceFirst("^/api/v\\d+", "");
    return WHITELIST.stream().anyMatch(pathWithoutVersion::startsWith);
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    return (bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7)
        : null);
  }

}
