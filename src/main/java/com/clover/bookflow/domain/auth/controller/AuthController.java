package com.clover.bookflow.domain.auth.controller;

import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.service.AuthService;
import com.clover.bookflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest signupRequest) {
    SignupResponse signupResponse = authService.signup(signupRequest);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(signupResponse));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest) {
    LoginResponse loginResponse = authService.login(loginRequest);
    return ResponseEntity
        .ok()
        .body(ApiResponse.success(loginResponse));
  }

}
