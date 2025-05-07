package com.clover.bookflow.domain.user.controller;

import static com.clover.bookflow.common.ApiDocs.combineFields;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiDocs;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.helper.UserTestHelper;
import com.clover.bookflow.domain.user.service.UserService;
import com.clover.bookflow.global.errorcode.UserErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import com.clover.bookflow.global.security.SecurityConfig;
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

// @WebMvcTest는 컨트롤러, 필터 등 웹 컴포넌트만 테스트하기 위한 어노테이션
// - MockMvc를 자동 설정하기 위해 내부적으로 @AutoConfigureMockMvc 포함
// - SecurityConfig는 명시적으로 @Import 필요
@WebMvcTest(UserController.class) // 컨트롤러만 스캔해서 스프링컨테이너에 등록
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 왜 @MockBean을 쓰는가? - @WebMvcTest는 Controller와 관련된 Spring MVC 컴포넌트만 로딩한다. - Controller는 Spring
   * MVC 환경(Web 요청/응답 흐름)에 의존하므로 Spring 컨텍스트가 필요하다. - @Mock은 단순 Mockito mock 객체 (Spring과 무관) -
   *
   * @MockBean / @MockitoBean은 Spring 컨텍스트 내 기존 빈을 mock으로 교체한다. - Spring Boot 3.4+부터는 @MockBean이
   * deprecated → @MockitoBean 사용 권장
   */
  @MockitoBean
  private UserService userService;

  @Autowired
  private WebApplicationContext context;

//  private RestDocumentationResultHandler restDocs;

  private UserTestHelper userTestHelper;
  private TestHelper testHelper;

  @BeforeEach
  void setUp(RestDocumentationContextProvider provider) {
//    this.restDocs = document("{class-name}/{method-name}"); // 문서 경로 설정 // 메서드마다 커스텀으로 변경
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(provider)) // RestDocs 구성
//        .alwaysDo(restDocs) // 문서 자동 생성
        .build();

    testHelper = new TestHelper(mockMvc, objectMapper);
    userTestHelper = new UserTestHelper();
  }

  @Nested
  @DisplayName("회원가입")
  class signup {

    @Nested
    @DisplayName("성공")
    class success {

      @DisplayName("회원가입을 요청하면 201 반환한다.")
      @Test
      void signup_success_201() throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createValidSignupRequest();
        UserSignupResponse response = userTestHelper.createSignupResponse();

        // when
        given(userService.signup(any(UserSignupRequest.class))).willReturn(response);
        // 여기 any 사용해도 request 는 mockMvc.perform 에 필요

        // then
        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isCreated()) // HTTP 201 기대
            .andDo(document("users/signup/success-201", // 문서 파일명
                resource(builder()
                    .tag("User") // Swagger UI 태그
                    .summary("회원가입 API") // 간단 설명
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields( // 요청 필드 설명
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        combineFields(
                            ApiDocs.COMMON_FIELDS,
                            fieldWithPath("data.email").description("회원 가입된 이메일"),
                            fieldWithPath("data.nickname").description("회원 가입된 닉네임")
                        )
                    )
                    .build()
                )
            ));
      }
      // DispatcherServlet 통해 요청 흐름
    }

    @Nested
    @DisplayName("실패")
    class fail {

      @ParameterizedTest
      @CsvSource(
          value = {
              "null, 'Email must not be blank'",
              "'', 'Email must not be blank'",
              "'invalid-email', 'Invalid email format'"
          },
          nullValues = "null"
      )
      public void testEmailValidation(String email, String expectedErrorMessage) throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createInvalidSignupRequest(email, "password123",
            "길똥이");

        // when & then
        String documentName = testHelper.generateDocName("email", email);

        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("users/signup/fail-400/email-validation-" + documentName,
                resource(builder()
                    .tag("User")
                    .summary("회원가입 API")
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        combineFields(
                            ApiDocs.COMMON_FIELDS,
                            ApiDocs.ERROR_DETAIL_FIELDS
                        )
                    )
                    .build()
                )
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
      public void testPasswordValidation(String password, String expectedErrorMessage)
          throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createInvalidSignupRequest("hkd111@example.com",
            password, "길똥이");

        // when & then
        String documentName = testHelper.generateDocName("password", password);

        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("users/signup/fail-400/password-validation-" + documentName,
                resource(builder()
                    .tag("User")
                    .summary("회원가입 API")
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        combineFields(
                            ApiDocs.COMMON_FIELDS,
                            ApiDocs.ERROR_DETAIL_FIELDS
                        )
                    )
                    .build()
                )
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
      public void testNicknameValidation(String nickname, String expectedErrorMessage)
          throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createInvalidSignupRequest("hkd111@example.com",
            "password123", nickname);

        // when & then
        String documentName = testHelper.generateDocName("nickname", nickname);

        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
            .andDo(document("users/signup/fail-400/nickname-validation-" + documentName,
                resource(builder()
                    .tag("User")
                    .summary("회원가입 API")
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        combineFields(
                            ApiDocs.COMMON_FIELDS,
                            ApiDocs.ERROR_DETAIL_FIELDS
                        )
                    )
                    .build()
                )
            ));
      }

      @DisplayName("이메일 중복이면 409를 반환한다.")
      @Test
      void signup_fail_409_when_duplicate_email() throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createValidSignupRequest();

        // when
        given(userService.signup(request)).willThrow(
            new DuplicateResourceException(UserErrorCode.EMAIL_ALREADY_EXISTS));

        // then
        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."))
            .andDo(document("users/signup/fail-409/duplicate-email",
                resource(builder()
                    .tag("User")
                    .summary("회원가입 API")
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        ApiDocs.COMMON_FIELDS
                    )
                    .build()
                )
            ));
      }

      @DisplayName("닉네임 중복이면 409를 반환한다.")
      @Test
      void signup_fail_409_when_duplicate_nickname() throws Exception {
        // given
        UserSignupRequest request = userTestHelper.createValidSignupRequest();

        // when
        given(userService.signup(request)).willThrow(
            new DuplicateResourceException(UserErrorCode.NICKNAME_ALREADY_EXISTS));

        // then
        testHelper.postRequest("/users/signup", request)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("NICKNAME_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."))
            .andDo(document("users/signup/fail-409/duplicate-nickname",
                resource(builder()
                    .tag("User")
                    .summary("회원가입 API")
                    .description("이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리합니다.")
                    .requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("password").description("사용자 비밀번호"),
                        fieldWithPath("nickname").description("사용자 닉네임")
                    )
                    .responseFields(
                        ApiDocs.COMMON_FIELDS
                    )
                    .build()
                )
            ));
      }

    }

  }


}
