package com.clover.bookflow.domain.auth.dto.response;

public record SignupResponse(

    MemberInfoResponse memberInfoResponse,
    AccessTokenResponse accessTokenResponse

) {

  public static SignupResponse from(MemberInfoResponse memberInfoResponse, String accessToken) {
    return new SignupResponse(
        memberInfoResponse, AccessTokenResponse.from(accessToken));
  }

}
