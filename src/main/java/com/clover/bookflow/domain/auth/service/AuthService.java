package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberService memberService;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;

  // 회원가입 시 jwt 토큰 발급
  public SignupResponse signup(SignupRequest signupRequest) {
    // 1. 사용자 정보 저장 (memberService 내부에서 중복 체크 및 인코딩 수행)
    Member member = memberService.signup(signupRequest);

    // 2. 회원가입이 성공하면 Authentication 객체 생성 (SecurityContext 에 저장되지는 않음)
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        member.getEmail(), signupRequest.password());

    // 3. JWT 토큰 생성
    String token = jwtProvider.createToken(authentication);

    return SignupResponse.from(member, token);

  }

  public LoginResponse login(LoginRequest loginRequest) {

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

      // 2. 인증된 사용자 정보 조회
      CustomMemberDetails userDetails = (CustomMemberDetails) auth.getPrincipal();
      Member member = memberRepository.findById(userDetails.getId())
          .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
          );

      // 3. JWT 토큰 생성
      String token = jwtProvider.createToken(auth);

      return LoginResponse.from(member, token);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(MemberErrorCode.LOGIN_FAILED);
    }
    /*
     * Spring Security의 기본 인증 흐름(DaoAuthenticationProvider)에서는 다음과 같이 동작:
     * 사용자를 찾기 위해 UserDetailsService.loadUserByUsername() 호출
     * 만약 유저가 없으면 → UsernameNotFoundException 발생
     * 그러나 기본 설정에서는 이 예외를 BadCredentialsException으로 래핑
     * */
  }


}
