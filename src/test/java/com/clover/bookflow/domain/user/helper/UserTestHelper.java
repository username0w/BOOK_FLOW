package com.clover.bookflow.domain.user.helper;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;

public class UserTestHelper {

  public UserSignupRequest createValidSignupRequest() {
    return new UserSignupRequest("hkd111@example.com", "password123", "길똥이");
  }

  public UserSignupRequest createInvalidSignupRequest(String email, String password, String name) {
    return new UserSignupRequest(email, password, name);
  }

  public UserSignupResponse createSignupResponse() {
    return new UserSignupResponse("hkd111@example.com", "길똥이");
  }

}
