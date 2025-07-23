package com.clover.bookflow.domain.auth.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiPath;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.SecurityConfig;
import com.clover.bookflow.docs.DocHelper;
import com.clover.bookflow.docs.auth.AuthDocs;
import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResult;
import com.clover.bookflow.domain.auth.dto.response.SignupResult;
import com.clover.bookflow.domain.auth.dto.response.TokenResult;
import com.clover.bookflow.domain.auth.security.JwtAuthenticationFilter;
import com.clover.bookflow.domain.auth.service.AuthService;
import com.clover.bookflow.domain.auth.token.service.TokenService;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.errorcode.TokenErrorCode;
import com.clover.bookflow.global.exception.BusinessException;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import com.clover.bookflow.global.exception.auth.InvalidTokenException;
import com.clover.bookflow.global.exception.auth.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

//@AutoConfigureMockMvc(addFilters = false) - 필터 무력화
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class AuthControllerTest {

  private static final String BASE_URL = ApiPath.AUTH;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  private WebApplicationContext context;

  private TestHelper testHelper;
  private AuthTestHelper authTestHelper;

  @BeforeEach
  void setUp(RestDocumentationContextProvider provider) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(provider))
        .build();

    testHelper = new TestHelper(mockMvc, objectMapper);
    authTestHelper = new AuthTestHelper();
  }

  @Nested
  @DisplayName("회원가입")
  class Signup {

    @Nested
    @DisplayName("성공")
    class Success {

      @DisplayName("회원가입 성공 시 201 Created 응답을 반환한다.")
      @Test
      void shouldReturn201_whenSignupSuccess() throws Exception {
        // given
        SignupRequest request = authTestHelper.createSignupRequest();
        SignupResult result = authTestHelper.createSignupResult();

        given(authService.signup(any(SignupRequest.class))).willReturn(result);
        // 여기 any 사용해도 request 는 mockMvc.perform 에 필요

        // when & then
        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isCreated()) // HTTP 201 기대
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.memberInfoResponse.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value("testNickname"))
            .andExpect(jsonPath("$.data.accessTokenResponse.accessToken").value("testAccessToken"))
            .andDo(document("auth/signup/success-201", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupSuccess()
                ))
            ));

        verify(authService).signup(request);
      }
      // DispatcherServlet 통해 요청 흐름
    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @ParameterizedTest
      @CsvSource(
          value = {
              "null, 'Email must not be blank'",
              "'', 'Email must not be blank'",
              "'invalid-email', 'Invalid email format'"
          },
          nullValues = "null"
      )
      void shouldReturnBadRequest_whenEmailIsInvalid(String email, String expectedErrorMessage)
          throws Exception {
        // given
        SignupRequest request = new SignupRequest(email, "password123", "testNickname");
        // test helper 사용 시 null 값이 기본값으로 변경됨. 대신 직접 객체 생성

        // when & then
        String documentName = testHelper.generateDocName("email", email);

        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("auth/signup/fail-400/email-validation-" + documentName,
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupError()
                ))
            ));

        verify(authService, never()).signup(any());
      }

      @ParameterizedTest
      @CsvSource(
          value = {
              "null, 'Password must not be blank'",
              "'          ', 'Password must not be blank'",
              "'0', 'Password must be between 8 and 20 characters'",
              "'abcdefghijklmnopqrstuvwxyz', 'Password must be between 8 and 20 characters'"
          },
          nullValues = "null"
      )
      void shouldReturnBadRequest_whenPasswordIsInvalid(String password,
          String expectedErrorMessage)
          throws Exception {
        // given
        SignupRequest request = new SignupRequest("test@example.com", password,
            "testNickname");

        // when & then
        String documentName = testHelper.generateDocName("password", password);

        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("auth/signup/fail-400/password-validation-" + documentName,
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupError()
                ))
            ));

        verify(authService, never()).signup(any());
      }

      @ParameterizedTest
      @CsvSource(
          value = {
              "null, 'Nickname must not be blank'",
              "'  ', 'Nickname must not be blank'",
              "'a', 'Nickname must be between 2 and 20 characters'",
              "'abcdefghijklmnopqrstuvwxyz', 'Nickname must be between 2 and 20 characters'"
          },
          nullValues = "null"
      )
      void shouldReturnBadRequest_whenNicknameIsInvalid(String nickname,
          String expectedErrorMessage)
          throws Exception {
        // given
        SignupRequest request = new SignupRequest("test@example.com",
            "password123", nickname);

        // when & then
        String documentName = testHelper.generateDocName("nickname", nickname);

        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("auth/signup/fail-400/nickname-validation-" + documentName,
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupError()
                ))
            ));

        verify(authService, never()).signup(any());
      }

      @DisplayName("이메일 중복 시 409 Conflict 응답을 반환한다")
      @Test
      void shouldReturnConflict_whenEmailIsDuplicate() throws Exception {
        // given
        String duplicatedEmail = "duplicate@example.com";
        SignupRequest request = authTestHelper.createInvalidSignupRequest(duplicatedEmail, null,
            null);

        given(authService.signup(request)).willThrow(
            new DuplicateResourceException(MemberErrorCode.EMAIL_ALREADY_EXISTS));

        // when & then
        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEMBER_004"))
            .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."))
            .andDo(document("auth/signup/fail-409/duplicate-email",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupError()
                ))
            ));

        verify(authService).signup(request);
      }

      @DisplayName("닉네임 중복 시 409 Conflict 응답을 반환한다")
      @Test
      void shouldReturnConflict_whenNicknameIsDuplicate() throws Exception {
        // given
        String duplicateNickname = "중복닉네임";
        SignupRequest request = authTestHelper.createInvalidSignupRequest(null, null,
            duplicateNickname);

        given(authService.signup(request)).willThrow(
            new DuplicateResourceException(MemberErrorCode.NICKNAME_ALREADY_EXISTS));

        // when & then
        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEMBER_005"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."))
            .andDo(document("auth/signup/fail-409/duplicate-nickname",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupError()
                ))
            ));

        verify(authService).signup(request);
      }
    }

  }

  @Nested
  @DisplayName("로그인")
  class Login {

    @Nested
    @DisplayName("성공")
    class Success {

      @DisplayName("로그인 성공 시 200 Ok 응답을 반환한다")
      @Test
      void shouldReturn200_whenLoginSuccess() throws Exception {
        // given
        LoginRequest request = authTestHelper.createLoginRequest();
        LoginResult result = authTestHelper.createLoginResult();
        given(authService.login(any(LoginRequest.class))).willReturn(result);

        // when & then
        testHelper.postRequest(BASE_URL + "/login", request)
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.memberInfoResponse.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value("testNickname"))
            .andExpect(jsonPath("$.data.accessTokenResponse.accessToken").value("testAccessToken"))
            .andDo(document("auth/login/success-200", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGIN_SUMMARY,
                    AuthDocs.LOGIN_DESCRIPTION,
                    AuthDocs.loginRequest(),
                    AuthDocs.loginSuccess()
                ))
            ));

        verify(authService).login(request);
      }
    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @DisplayName("잘못된 비밀번호 입력 시 401 Unauthorized 응답을 반환한다")
      @Test
      void shouldReturnUnauthorized_whenWrongPassword() throws Exception {
        // given
        LoginRequest request = authTestHelper.createLoginRequest();
        given(authService.login(request)).willThrow(
            new UnauthorizedException(MemberErrorCode.LOGIN_FAILED));

        // when & then
        testHelper.postRequest(BASE_URL + "/login", request)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEMBER_003"))
            .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
            .andDo(document("auth/login/fail-401/wrong-password", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGIN_SUMMARY,
                    AuthDocs.LOGIN_DESCRIPTION,
                    AuthDocs.loginRequest(),
                    AuthDocs.loginError()
                ))
            ));

        verify(authService).login(request);
      }

      @DisplayName("등록되지 않은 이메일 입력 시 401 Unauthorized 응답을 반환한다")
      @Test
      void shouldReturnUnauthorized_whenInvalidEmail() throws Exception {
        // given
        LoginRequest request = authTestHelper.createLoginRequest();
        given(authService.login(request)).willThrow(
            new UnauthorizedException(MemberErrorCode.LOGIN_FAILED));

        // when & then
        testHelper.postRequest(BASE_URL + "/login", request)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEMBER_003"))
            .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
            .andDo(document("auth/login/fail-401/invalid-email", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGIN_SUMMARY,
                    AuthDocs.LOGIN_DESCRIPTION,
                    AuthDocs.loginRequest(),
                    AuthDocs.loginError()
                ))
            ));

        verify(authService).login(request);
      }
    }

  }

  @Nested
  @DisplayName("토큰 재발급")
  class Refresh {

    @Nested
    @DisplayName("성공")
    class Success {

      @DisplayName("유효한 Refresh Token으로 토큰을 재발급하면, 새로운 Access Token과 Refresh Token이 발급된다")
      @Test
      void shouldReturnNewTokenPair_whenValidRefreshToken() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";

        TokenResult tokenResult = authTestHelper.createTokenResult();

        String newAccessToken = tokenResult.accessToken().token();
        String newRefreshToken = tokenResult.refreshToken().token();

        given(tokenService.refreshToken(oldRefreshToken)).willReturn(tokenResult);

        // when & then
        testHelper.postRequestWithToken(BASE_URL + "/refresh", oldRefreshToken)
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE,
                containsString("refreshToken=" + newRefreshToken)))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
            .andDo(document("auth/refresh/success-200", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.REFRESH_SUMMARY,
                    AuthDocs.REFRESH_DESCRIPTION,
                    null,
                    AuthDocs.refreshSuccess()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("재발급에 사용되는 Refresh Token")
                ),
                responseCookies(
                    cookieWithName("refreshToken").description("새로 발급된 Refresh Token")
                )
            ));

        verify(tokenService).refreshToken(oldRefreshToken);
      }
    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @DisplayName("Refresh Token 이 빈 값이면 400 Bad Request 응답을 반환한다.")
      @ParameterizedTest
      @ValueSource(strings = {"", "   "})
      void shouldReturnBadRequest_whenRefreshTokenIsBlank(String refreshToken)
          throws Exception {
        String documentName = testHelper.generateDocName("token", refreshToken);

        testHelper.postRequestWithToken(BASE_URL + "/refresh", refreshToken)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(TokenErrorCode.TOKEN_NOT_PRESENT.getCode()))
            .andExpect(jsonPath("$.message").value(TokenErrorCode.TOKEN_NOT_PRESENT.getMessage()))
            .andDo(document("auth/refresh/fail-400/token-not-present-" + documentName,
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.REFRESH_SUMMARY,
                    AuthDocs.REFRESH_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("재발급에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService, never()).refreshToken(any());
      }

      @DisplayName("DB 에 없는 토큰으로 재발급 요청하면 401 Unauthorized 응답을 반환한다")
      @Test
      void shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        // given
        String invalidToken = "invalid-token";

        given(tokenService.refreshToken(invalidToken))
            .willThrow(new InvalidTokenException(TokenErrorCode.TOKEN_NOT_FOUND));

        // when & then
        testHelper.postRequestWithToken(BASE_URL + "/refresh", invalidToken)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(TokenErrorCode.TOKEN_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(TokenErrorCode.TOKEN_NOT_FOUND.getMessage()))
            .andDo(document("auth/refresh/fail-404/token-not-found",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.REFRESH_SUMMARY,
                    AuthDocs.REFRESH_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("재발급에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService).refreshToken(invalidToken);
      }

      @DisplayName("만료된 토큰으로 재발급 요청 시 401 Unauthorized 반환")
      @Test
      void shouldReturnUnauthorized_whenTokenExpired() throws Exception {
        // given
        String expiredToken = "expired-token";
        given(tokenService.refreshToken(expiredToken)).willThrow(new TokenExpiredException());

        // when & then
        testHelper.postRequestWithToken(BASE_URL + "/refresh", expiredToken)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("TOKEN_001"))
            .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."))
            .andDo(document("auth/refresh/fail-401/token-expired",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.REFRESH_SUMMARY,
                    AuthDocs.REFRESH_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("재발급에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService).refreshToken(expiredToken);
      }

      @DisplayName("DB에 없는 멤버로 인한 토큰 재발급 요청 시 404 Not Found 반환")
      @Test
      void shouldReturnNotFound_whenMemberNotFound() throws Exception {
        String tokenWithMissingMember = "token-without-member";

        given(tokenService.refreshToken(tokenWithMissingMember)).willThrow(
            new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        testHelper.postRequestWithToken(BASE_URL + "/refresh", tokenWithMissingMember)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEMBER_001"))
            .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."))
            .andDo(document("auth/refresh/fail-401/token-without-member",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.REFRESH_SUMMARY,
                    AuthDocs.REFRESH_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("재발급에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService).refreshToken(tokenWithMissingMember);
      }
    }
  }

  @Nested
  @DisplayName("로그아웃")
  class Logout {

    @Nested
    @DisplayName("성공")
    class Success {

      @DisplayName("유효한 토큰으로 로그아웃하면 jti 로 토큰을 삭제한다")
      @Test
      void shouldDeleteToken_whenValidToken() throws Exception {
        // given
        String validRefreshToken = "refresh-token";

        // when & then
        testHelper.postRequestWithToken(BASE_URL + "/logout", validRefreshToken)
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(header().string(HttpHeaders.SET_COOKIE,
                allOf(
                    containsString("refreshToken=;"),
                    containsString("Max-Age=0")
                )
            ))
            .andExpect(jsonPath("$.success").value(true))
            .andDo(document("auth/logout/success-200",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGOUT_SUMMARY,
                    AuthDocs.LOGOUT_DESCRIPTION,
                    null,
                    AuthDocs.logoutSuccess()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("로그아웃에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService).logout(validRefreshToken);
      }

    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @DisplayName("Refresh Token 이 빈 값이면 400 Bad Request 응답을 반환한다.")
      @ParameterizedTest
      @ValueSource(strings = {"", "   "})
      void shouldReturnBadRequest_whenRefreshTokenIsBlank(String refreshToken)
          throws Exception {

        testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(TokenErrorCode.TOKEN_NOT_PRESENT.getCode()))
            .andExpect(jsonPath("$.message").value(TokenErrorCode.TOKEN_NOT_PRESENT.getMessage()))
            .andDo(document("auth/logout/fail-400/blank-token",
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGOUT_SUMMARY,
                    AuthDocs.LOGOUT_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("로그아웃에 사용되는 Refresh Token")
                )
            ));

        verify(tokenService, never()).logout(any());
      }

      @DisplayName("잘못된 형식의 토큰이면 401 Unauthorized 응답을 반환한다.")
      @ParameterizedTest
      @ValueSource(strings = {"abcde", "1234.abcd.5678"})
      void shouldReturnUnauthorized_whenRefreshTokenIsInvalidFormat(String refreshToken)
          throws Exception {
        doThrow(new InvalidTokenException(TokenErrorCode.INVALID_TOKEN))
            .when(tokenService).logout(refreshToken);

        String documentName = testHelper.generateDocName("token", refreshToken);

        testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(TokenErrorCode.INVALID_TOKEN.getCode()))
            .andExpect(jsonPath("$.message").value(TokenErrorCode.INVALID_TOKEN.getMessage()))
            .andDo(document("auth/logout/fail-401/invalid-format-token-" + documentName,
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGOUT_SUMMARY,
                    AuthDocs.LOGOUT_DESCRIPTION,
                    null,
                    AuthDocs.error()
                )),
                requestCookies(
                    cookieWithName("refreshToken").description("로그아웃에 사용되는 Refresh Token")
                )
            )).andDo(print());
        ;

        verify(tokenService).logout(refreshToken);
      }
    }
  }


}
