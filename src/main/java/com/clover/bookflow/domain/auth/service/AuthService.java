package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.domain.TokenPair;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResult;
import com.clover.bookflow.domain.auth.dto.response.SignupResult;
import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.auth.token.service.TokenService;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private final MemberService memberService;
  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  // 회원가입 시 jwt 토큰 발급
  public SignupResult signup(SignupRequest signupRequest) {
    // 1. 사용자 정보 저장 (memberService 내부에서 중복 체크 및 인코딩 수행)
    Member member = memberService.signup(signupRequest);

    // 2. 권한 추출
    List<String> roles = List.of("ROLE_" + member.getRole().name());

    // => JWT stateless 방식을 위해 기존 수등 등록 방식에서 클라이언트가 응답 토큰으로 요청하도록 변경

    // 3. JWT 토큰 생성
    TokenPair tokenPair = tokenService.issueTokens(member.getUuid(), roles);

    // 4. RefreshToken 저장
    refreshTokenRepository.save(RefreshToken.create(member, tokenPair.refreshToken()));

    return SignupResult.from(member, tokenPair);

  }

  public LoginResult login(LoginRequest loginRequest) {

    // authenticationManager.authenticate(...) 호출
    // 이 메서드는 내부적으로 AuthenticationProvider(보통 DaoAuthenticationProvider) 사용
    // DaoAuthenticationProvider는
    // UserDetailsService를 사용해서 사용자 정보를 로드하고, 비밀번호를 검증
    // 사용자 조회 : UserDetailsService 구현체
    // 비밀번호 검증 : Spring Security 내부
    // 인증 처리 : AuthenticationManager.authenticate(...)
    // 인증 토큰 생성
    // 인증 객체 생성
    // 1. 로그인 인증 시도 (이 과정에서 UserDetailsService가 호출됨)
    try {
      Authentication auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
      );

      log.info("Authentication successful for user: {}", loginRequest.email());

      // 2. 인증된 사용자 정보 조회
      CustomMemberDetails userDetails = (CustomMemberDetails) auth.getPrincipal();
      Member member = memberRepository.findById(userDetails.getId())
          .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
          );

      List<String> roles = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .toList();

      // 3. JWT 토큰 생성
      TokenPair tokenPair = tokenService.issueTokens(userDetails.getUuid(), roles);

      // refreshToken 저장
      refreshTokenRepository.save(RefreshToken.create(member, tokenPair.refreshToken()));

      return LoginResult.from(member, tokenPair);
    } catch (BadCredentialsException e) {
      log.warn("Authentication failed for user: {}", loginRequest.email());
      throw new UnauthorizedException(MemberErrorCode.LOGIN_FAILED);
    }
    /*
     * Spring Security의 기본 인증 흐름(DaoAuthenticationProvider)에서는 다음과 같이 동작:
     * 사용자를 찾기 위해 UserDetailsService.loadUserByUsername() 호출
     * 만약 유저가 없으면 → UsernameNotFoundException 발생
     * 그러나 기본 설정에서는 이 예외를 BadCredentialsException으로 래핑
     * */
  }

  // 회원가입 후 바로 토큰 반환, 로그인도 토큰 반환 => 별도 메서드로 분리 => TokenService

}
