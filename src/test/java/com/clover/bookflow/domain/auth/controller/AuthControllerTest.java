package com.clover.bookflow.domain.auth.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiPath;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.SecurityConfig;
import com.clover.bookflow.docs.DocHelper;
import com.clover.bookflow.docs.auth.AuthDocs;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.dto.response.LoginResponse;
import com.clover.bookflow.domain.auth.dto.response.SignupResponse;
import com.clover.bookflow.domain.auth.security.JwtAuthenticationFilter;
import com.clover.bookflow.domain.auth.service.AuthService;
import com.clover.bookflow.domain.member.helper.MemberTestHelper;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  private WebApplicationContext context;

  private TestHelper testHelper;
  private MemberTestHelper memberTestHelper;

  @BeforeEach
  void setUp(RestDocumentationContextProvider provider) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(provider))
        .build();

    testHelper = new TestHelper(mockMvc, objectMapper);
    memberTestHelper = new MemberTestHelper();
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
        SignupRequest request = memberTestHelper.createSignupRequest();
        SignupResponse response = memberTestHelper.createSignupResponse();

        given(authService.signup(any(SignupRequest.class))).willReturn(response);
        // 여기 any 사용해도 request 는 mockMvc.perform 에 필요

        // when & then
        testHelper.postRequest(BASE_URL + "/signup", request)
            .andExpect(status().isCreated()) // HTTP 201 기대
            .andDo(document("auth/signup/success-201", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.SIGNUP_SUMMARY,
                    AuthDocs.SIGNUP_DESCRIPTION,
                    AuthDocs.signupRequest(),
                    AuthDocs.signupSuccess()
                ))
            ));
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
      }

      @DisplayName("이메일 중복 시 409 Conflict 응답을 반환한다")
      @Test
      void shouldReturnConflict_whenEmailIsDuplicate() throws Exception {
        // given
        String duplicatedEmail = "duplicate@example.com";
        SignupRequest request = memberTestHelper.createInvalidSignupRequest(duplicatedEmail, null,
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
      }

      @DisplayName("닉네임 중복 시 409 Conflict 응답을 반환한다")
      @Test
      void shouldReturnConflict_whenNicknameIsDuplicate() throws Exception {
        // given
        String duplicateNickname = "중복닉네임";
        SignupRequest request = memberTestHelper.createInvalidSignupRequest(null, null,
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
        LoginRequest request = memberTestHelper.createLoginRequest();
        LoginResponse response = memberTestHelper.createLoginResponse();
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        testHelper.postRequest(BASE_URL + "/login", request)
            .andExpect(status().isOk())
            .andDo(document("auth/login/success-200", // 문서 파일명
                resource(DocHelper.build(
                    AuthDocs.TAG,
                    AuthDocs.LOGIN_SUMMARY,
                    AuthDocs.LOGIN_DESCRIPTION,
                    AuthDocs.loginRequest(),
                    AuthDocs.loginSuccess()
                ))
            ));

      }
    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @DisplayName("잘못된 비밀번호 입력 시 401 Unauthorized 응답을 반환한다")
      @Test
      void shouldReturnUnauthorized_whenWrongPassword() throws Exception {
        // given
        LoginRequest request = memberTestHelper.createLoginRequest();
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

      }

      @DisplayName("등록되지 않은 이메일 입력 시 401 Unauthorized 응답을 반환한다")
      @Test
      void shouldReturnUnauthorized_whenInvalidEmail() throws Exception {
        // given
        LoginRequest request = memberTestHelper.createLoginRequest();
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

      }


    }

  }

}
