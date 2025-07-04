package com.clover.bookflow.domain.auth.dto.response;

public record LoginResponse(
    MemberInfoResponse memberInfoResponse
) {

  public static LoginResponse from(MemberInfoResponse memberInfoResponse) {
    return new LoginResponse(
        memberInfoResponse
    );
  }

}
