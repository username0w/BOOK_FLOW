package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.security.CustomUserDetails;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import com.clover.bookflow.domain.user.service.UserService;
import com.clover.bookflow.global.errorcode.UserErrorCode;
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

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;

  // 회원가입 시 jwt 토큰 발급
  public SignupResponse signup(SignupRequest signupRequest) {
    // controller 에서 userService.signup 호출하고 내부에서 authService.signup 호출
    // 1. 사용자 정보 저장
    User user = userService.signup(signupRequest);

    // 2. 회원가입이 성공하면 Authentication 객체 생성
    // 회원가입 후 이메일을 이용해 인증 객체 생성 (비밀번호는 저장되어 있어야 함)
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        user.getEmail(), signupRequest.password()); // 이메일과 비밀번호로 인증 객체 생성

    // 3. 토큰 생성
    String token = jwtProvider.createToken(authentication); // 인증 객체를 넘겨서 토큰 생성

    return SignupResponse.from(user, token);

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
    try {
      Authentication auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
      ); // 로그인 인증 시도

      CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
      User user = userRepository.findById(userDetails.getId())
          .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND)
          );
      String token = jwtProvider.createToken(auth);

      // 토큰 돌려주기
      return LoginResponse.from(user, token);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(UserErrorCode.LOGIN_FAILED);
    }
    /*
     * Spring Security의 기본 인증 흐름(DaoAuthenticationProvider)에서는 다음과 같이 동작:
     * 사용자를 찾기 위해 UserDetailsService.loadUserByUsername() 호출
     * 만약 유저가 없으면 → UsernameNotFoundException 발생
     * 그러나 기본 설정에서는 이 예외를 BadCredentialsException으로 래핑
     * */
  }


}
