package com.clover.bookflow.domain.user.controller;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<UserSignupResponse>> signup(
      @Valid @RequestBody UserSignupRequest userSignupRequest) {
    UserSignupResponse userSignupResponse = userService.signup(userSignupRequest);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(userSignupResponse));
  }

}
