package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.dto.AuthResponse;
import com.clover.bookflow.domain.auth.dto.LoginRequest;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.service.UserService;
import com.clover.bookflow.global.errorcode.UserErrorCode;
import com.clover.bookflow.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;

  // 회원가입 시 jwt 토큰 발급
  public AuthResponse signup(UserSignupRequest userSignupRequest) {
    // controller 에서 userService.signup 호출하고 내부에서 authService.signup 호출
    // 1. 사용자 정보 저장
    UserSignupResponse userSignupResponse = userService.signup(userSignupRequest);

    // 2. 회원가입이 성공하면 Authentication 객체 생성
    // 회원가입 후 이메일을 이용해 인증 객체 생성 (비밀번호는 저장되어 있어야 함)
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userSignupResponse.email(), userSignupRequest.password()); // 이메일과 비밀번호로 인증 객체 생성

    // 3. 토큰 생성
    String token = jwtProvider.createToken(authentication); // 인증 객체를 넘겨서 토큰 생성

    return AuthResponse.from(userSignupResponse, token);

  }

  public AuthResponse login(LoginRequest loginRequest) {

    // authenticationManager.authenticate(...) 호출
    // 이 메서드는 내부적으로 AuthenticationProvider(보통 DaoAuthenticationProvider) 사용
    // DaoAuthenticationProvider는
    // UserDetailsService를 사용해서 사용자 정보를 로드하고, 비밀번호를 검증
    // 사용자 조회 : UserDetailsService 구현체
    // 비밀번호 검증 : Spring Security 내부
    // 인증 처리 : AuthenticationManager.authenticate(...)
    // 인증 토큰 생성
    // 인증 객체 생성
    try {
      Authentication auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
      ); // 로그인 인증 시도

      User user = (User) auth.getPrincipal();
      String token = jwtProvider.createToken(auth);

      // 토큰 돌려주기
      return AuthResponse.from(UserSignupResponse.from(user), token);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(UserErrorCode.PASSWORD_MISMATCH);
    } catch (UsernameNotFoundException e) {
      throw new UnauthorizedException(UserErrorCode.USER_NOT_FOUND);
    }
  }


}
