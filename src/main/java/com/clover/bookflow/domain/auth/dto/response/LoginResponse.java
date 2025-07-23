package com.clover.bookflow.domain.auth.dto.response;

public record LoginResponse(
    MemberInfoResponse memberInfoResponse,
    AccessTokenResponse accessTokenResponse
) {

  public static LoginResponse from(MemberInfoResponse memberInfoResponse, String accessToken) {
    return new LoginResponse(
        memberInfoResponse, AccessTokenResponse.from(accessToken)
    );
  }

}
