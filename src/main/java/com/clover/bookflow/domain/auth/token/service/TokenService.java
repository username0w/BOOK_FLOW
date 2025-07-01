package com.clover.bookflow.domain.auth.token.service;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.domain.auth.dto.response.TokenResponse;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.errorcode.TokenErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.auth.InvalidTokenException;
import com.clover.bookflow.global.exception.auth.TokenExpiredException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;


  // 회원가입 후 바로 토큰 반환, 로그인도 토큰 반환 => 별도 메서드로 분리
  // AccessToken 재발급 시 RefreshToken 도 함께 재발급
  public TokenPair issueTokens(String email, List<String> roles) {
    TokenWithMeta accessToken = jwtProvider.createAccessToken(email, roles);
    TokenWithMeta refreshToken = jwtProvider.createRefreshToken(email, roles);

    return TokenPair.of(accessToken, refreshToken);
  }

  public TokenResponse reissueToken(String refreshToken) {
    // jti 추출해서 DB 에 저장된 값과 매칭
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    RefreshToken savedToken = refreshTokenRepository.findByJti(jti)
        .orElseThrow(() -> new InvalidTokenException(TokenErrorCode.TOKEN_NOT_FOUND));

    // 토큰 만료 확인
    if (savedToken.isExpired()) {
      throw new TokenExpiredException();
    }

    // 사용자 정보 조회
    String email = jwtProvider.getEmailFromToken(refreshToken);
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

    List<String> roles = List.of("ROLE_" + member.getRole().name());

    // 토큰 새로 발급
    TokenPair tokenPair = issueTokens(member.getEmail(), roles);

    // 기존 토큰 갱신
    TokenWithMeta newRefreshToken = tokenPair.refreshToken();
    savedToken.update(newRefreshToken.token(), newRefreshToken.expiresAt(), newRefreshToken.jti());
    refreshTokenRepository.save(savedToken);

    return TokenResponse.from(tokenPair);
  }

  // Todo : logout 시 토큰 삭제
  public void logout(String refreshToken) {
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    refreshTokenRepository.deleteByJti(jti);
  }


}
