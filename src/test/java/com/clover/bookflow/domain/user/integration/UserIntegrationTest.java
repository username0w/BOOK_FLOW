package com.clover.bookflow.domain.user.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.user.dto.request.UserSignupRequest;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends AbstractIntegrationTest {

  // 단위테스트와 달리 통합테스트에서 필드 주입한 이유
  // 이유1. 생성자 주입이 통합에서 큰 이점 없다.
  // 통합테스트에서는 Spring 이 자동으로 모든 Bean 구성, 주입
  // Mock 객체나 직접 주입할 필요 적음
  // 생성자 주입의 장점(명확한 의존성, 불변성) 은 서비스/비즈니스 로직 쪽에서 훨씬 중요
  // 2. 테스트 코드의 가독성과 단순화
  // 생성자 만들고 this 할당보다 필드 주입이 더 간결, 덜 방해

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void clean() {
    userRepository.deleteAll();
  }

  @Nested
  @DisplayName("회원가입")
  class signup {

    @DisplayName("회원가입 성공")
    @Test
    void signup_success() throws Exception {
      UserSignupRequest request = new UserSignupRequest("hkd111@example.com", "password123", "길똥이");
      mockMvc.perform(post("/users/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf())
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.email").value("hkd111@example.com"))
          .andExpect(jsonPath("$.data.nickname").value("길똥이"));
    }

    @DisplayName("회원가입 실패 - 중복 이메일로 요청")
    @Test
    void signup_duplicate_email_fail() throws Exception {
      // given
      userRepository.save(User.create("hkd111@example.com", "encodedPW", "길똥이"));

      UserSignupRequest request = new UserSignupRequest("hkd111@example.com", "password123", "길똥이");

      // when, then
      mockMvc.perform(post("/users/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf())
          )
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
          .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @DisplayName("회원가입 실패 - 중복 닉네임으로 요청")
    @Test
    void signup_duplicate_nickname_fail() throws Exception {
      // given
      userRepository.save(User.create("hkd111@example.com", "encodedPW", "길똥이"));

      UserSignupRequest request = new UserSignupRequest("hkd222@example.com", "password123", "길똥이");

      // when, then
      mockMvc.perform(post("/users/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf())
          )
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("NICKNAME_ALREADY_EXISTS"))
          .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }


  }


}
