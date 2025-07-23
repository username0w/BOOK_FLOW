package com.clover.bookflow.domain.member.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.SecurityConfig;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
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
@WebMvcTest(MemberController.class) // 컨트롤러만 스캔해서 스프링컨테이너에 등록
@Import(SecurityConfig.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class MemberControllerTest {

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
  private MemberService memberService;

  @Autowired
  private WebApplicationContext context;

//  private RestDocumentationResultHandler restDocs;

  private MemberTestHelper memberTestHelper;
  private TestHelper testHelper;

  @BeforeEach
  void setUp(RestDocumentationContextProvider provider) {
//    this.restDocs = document("{class-name}/{method-name}"); // 문서 경로 설정 // 메서드마다 커스텀으로 변경
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(provider)) // RestDocs 구성
//        .alwaysDo(restDocs) // 문서 자동 생성
        .build();

    testHelper = new TestHelper(mockMvc, objectMapper);
    memberTestHelper = new MemberTestHelper();
  }


}
