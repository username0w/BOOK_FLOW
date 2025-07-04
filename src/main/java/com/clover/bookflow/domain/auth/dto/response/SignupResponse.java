package com.clover.bookflow.domain.auth.dto.response;

public record SignupResponse(

    MemberInfoResponse memberInfoResponse

) {

  public static SignupResponse from(MemberInfoResponse memberInfoResponse) {
    return new SignupResponse(
        memberInfoResponse);
  }

}
