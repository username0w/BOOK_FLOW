package com.clover.bookflow.domain.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.dto.response.UserSignupResponse;
import com.clover.bookflow.domain.user.service.UserService;
import com.clover.bookflow.global.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

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

  @BeforeEach
  void setUp(RestDocumentationContextProvider provider) {
//    this.restDocs = document("{class-name}/{method-name}"); // 문서 경로 설정 // 메서드마다 커스텀으로 변경
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(provider)) // RestDocs 구성
//        .alwaysDo(restDocs) // 문서 자동 생성
        .build();
  }

  @DisplayName("회원가입을 요청하면 201 반환한다.")
  @Test
  void signup_success_201() throws Exception {
    // given
    UserSignupRequest request = new UserSignupRequest("hkd111@example.com", "password123", "길똥이");
    UserSignupResponse response = new UserSignupResponse("hkd111@example.com", "길똥이");

    // when
    given(userService.signup(any(UserSignupRequest.class))).willReturn(response);

    // then
    mockMvc.perform(post("/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf())) // CSRF 토큰 포함 (Spring Security)
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
                .build()
            )
        ));
  }
  // DispatcherServlet 통해 요청 흐름


}
