package com.clover.bookflow.domain.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final UserDetailsService userDetailsService;


  // 요청마다 실행됨: JWT 검증 및 사용자 인증 처리
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = jwtProvider.resolveToken(request); // 헤더에서 토큰 추출

    if (token != null && jwtProvider.validateToken(token)) { // 토큰 유효성 검증
      String email = jwtProvider.getEmailFromToken(token); // 토큰에서 이메일 추출
      UserDetails userDetails = userDetailsService.loadUserByUsername(email); // 사용자 조회

      // 인증 객체 생성 및 설정
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 저장
    }

    filterChain.doFilter(request, response); // 다음 필터로 넘김
  }
}
