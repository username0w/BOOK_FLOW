package com.clover.bookflow.common;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class TestHelper {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;

  public TestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  // post 요청
  public ResultActions postRequest(String url, Object request) throws Exception {
    return performRequest(post(url), request);
  }


  // 공통 요청
  private ResultActions performRequest(MockHttpServletRequestBuilder builder, Object request)
      throws Exception {
    return mockMvc.perform(
        builder
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(request)) // 자바 객체를 JSON 문자열로 변환하여 CONTENT 에 넣기 위함.
            .with(csrf()) // CSRF 토큰 포함 (Spring Security)
    );
  }
  // .content 는 byte[] 타입 받는 메서드
  // String 넘기면 내부에서 .getBytes() 처리한다.
  // = 문자열, JSON, XML, 폼 데이터 전부 가능

  // 토큰 재발급, 로그아웃 전용
  public ResultActions postRequestWithToken(String url, String token) throws Exception {
    return mockMvc.perform(
        post(url)
            .cookie(new Cookie("refreshToken", token)));
  }

  // 문서화 파일 이름
  public String generateDocName(String fieldName, String value) {
    if (value == null) {
      return fieldName + "-null";
    }
    if (value.isEmpty()) {
      return fieldName + "-empty";
    }
    return fieldName + "-" + value.replaceAll("[^a-zA-Z0-9]", "-");
  }
}
