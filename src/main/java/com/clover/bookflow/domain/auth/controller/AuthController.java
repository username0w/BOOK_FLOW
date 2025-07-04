package com.clover.bookflow.domain.auth.controller;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.LoginResult;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResult;
import com.clover.bookflow.domain.auth.dto.response.TokenResponse;
import com.clover.bookflow.domain.auth.service.AuthService;
import com.clover.bookflow.domain.auth.token.service.TokenService;
import com.clover.bookflow.domain.auth.util.CookieUtil;
import com.clover.bookflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final TokenService tokenService;

  @Value("${jwt.access-token-expiration-ms}")
  private long accessTokenExpiry;

  @Value("${jwt.refresh-token-expiration-ms}")
  private long refreshTokenExpiry;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest signupRequest) {
    SignupResult signupResult = authService.signup(signupRequest);

    String accessToken = signupResult.tokenResponse().accessToken().token();
    String refreshToken = signupResult.tokenResponse().refreshToken().token();

    String accessTokenCookieString = CookieUtil.createAccessTokenCookie(accessToken,
        (int) (accessTokenExpiry / 1000));
    String refreshTokenCookieString = CookieUtil.createRefreshTokenCookie(refreshToken,
        (int) (refreshTokenExpiry / 1000));

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, accessTokenCookieString);
    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookieString);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .headers(headers)
        .body(ApiResponse.created(SignupResponse.from(signupResult.memberInfoResponse())));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest) {
    LoginResult loginResult = authService.login(loginRequest);

    String accessToken = loginResult.tokenResponse().accessToken().token();
    String refreshToken = loginResult.tokenResponse().refreshToken().token();

    String accessTokenCookieString = CookieUtil.createAccessTokenCookie(accessToken,
        (int) (accessTokenExpiry / 1000));
    String refreshTokenCookieString = CookieUtil.createRefreshTokenCookie(refreshToken,
        (int) (refreshTokenExpiry / 1000));

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, accessTokenCookieString);
    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookieString);

    return ResponseEntity
        .ok()
        .headers(headers)
        .body(ApiResponse.success(LoginResponse.from(loginResult.memberInfoResponse())));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<String>> refreshToken(
      @CookieValue(name = "refreshToken", required = false) String refreshToken
  ) {

    TokenResponse tokenResponse = tokenService.refreshToken(refreshToken);

    String accessToken = tokenResponse.accessToken().token();
    String newRefreshToken = tokenResponse.refreshToken().token();

    String accessTokenCookieString = CookieUtil.createAccessTokenCookie(accessToken,
        (int) (accessTokenExpiry / 1000));
    String refreshTokenCookieString = CookieUtil.createRefreshTokenCookie(newRefreshToken,
        (int) (refreshTokenExpiry / 1000));

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, accessTokenCookieString);
    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookieString);

    return ResponseEntity.ok()
        .headers(headers)
        .body(ApiResponse.success("Token refreshed"));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken
  ) {
    String clearAccess = CookieUtil.deleteTokenCookie("accessToken");
    String clearRefresh = CookieUtil.deleteTokenCookie("refreshToken");
    tokenService.logout(refreshToken);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, clearAccess);
    headers.add(HttpHeaders.SET_COOKIE, clearRefresh);

    return ResponseEntity.ok()
        .headers(headers)
        .body(ApiResponse.success("Logout success"));
  }


}
