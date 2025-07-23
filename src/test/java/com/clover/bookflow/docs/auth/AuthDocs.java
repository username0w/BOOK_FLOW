package com.clover.bookflow.docs.auth;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.clover.bookflow.docs.ApiDocs;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;

public class AuthDocs {

  public static final String TAG = "Auth";
  public static final String SIGNUP_SUMMARY = "회원가입 API";
  public static final String SIGNUP_DESCRIPTION = "이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.";
  public static final String LOGIN_SUMMARY = "로그인 API";
  public static final String LOGIN_DESCRIPTION = "이메일, 비밀번호를 입력받아 로그인을 처리합니다.";
  public static final String REFRESH_SUMMARY = "토큰 재발급 API";
  public static final String REFRESH_DESCRIPTION = "유효한 Refresh Token 으로 새로운 Access/Refresh Token 을 발급합니다.";
  public static final String LOGOUT_SUMMARY = "로그아웃 API";
  public static final String LOGOUT_DESCRIPTION = "유효한 Refresh Token 으로 로그아웃을 처리합니다.";


  public static List<FieldDescriptor> accessTokenFields() {
    return List.of(
        fieldWithPath("data.accessTokenResponse.accessToken").description("토큰 값")
    );
  }

  public static List<FieldDescriptor> signupRequest() {
    return List.of(
        fieldWithPath("email").description("이메일"),
        fieldWithPath("password").description("비밀번호"),
        fieldWithPath("nickname").description("닉네임")
    );
  }

  public static List<FieldDescriptor> signupSuccess() {
    return ApiDocs.combine(
        ApiDocs.commonFields(),
        List.of(
            fieldWithPath("data.memberInfoResponse.email").description("가입된 이메일"),
            fieldWithPath("data.memberInfoResponse.nickname").description("가입된 닉네임")
        ),
        accessTokenFields()
    );
  }

  public static List<FieldDescriptor> signupError() {
    return ApiDocs.combine(
        ApiDocs.commonFields(),
        ApiDocs.errorDetailFields()
    );
  }

  public static List<FieldDescriptor> loginRequest() {
    return List.of(
        fieldWithPath("email").description("이메일"),
        fieldWithPath("password").description("비밀번호")
    );
  }

  public static List<FieldDescriptor> loginSuccess() {
    return ApiDocs.combine(
        ApiDocs.commonFields(),
        List.of(
            fieldWithPath("data.memberInfoResponse.email").description("로그인된 이메일"),
            fieldWithPath("data.memberInfoResponse.nickname").description("로그인된 닉네임")
        ),
        accessTokenFields()
    );
  }

  public static List<FieldDescriptor> loginError() {
    return ApiDocs.combine(
        ApiDocs.commonFields(),
        ApiDocs.errorDetailFields()
    );
  }

  public static List<FieldDescriptor> refreshSuccess() {
    return ApiDocs.combine(
        ApiDocs.commonFields(),
        List.of(fieldWithPath("data.accessToken").description("토큰 값"))
    );
  }

  public static List<FieldDescriptor> logoutSuccess() {
    return ApiDocs.commonFields();
  }


  public static List<FieldDescriptor> error() {
    return ApiDocs.commonFields();
  }


}

