package com.clover.bookflow.domain.auth.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiPath;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.helper.UserTestHelper;
import com.clover.bookflow.domain.user.repository.UserRepository;
import com.clover.bookflow.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {

  private static final String BASE_URL = ApiPath.AUTH;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private TestHelper testHelper;
  private UserTestHelper userTestHelper;

  @BeforeEach
  void clean() {
    userRepository.deleteAll();

    testHelper = new TestHelper(mockMvc, objectMapper);
    userTestHelper = new UserTestHelper();
  }

  @Nested
  @DisplayName("회원가입")
  class Signup {

    @DisplayName("회원가입 성공 시 201 Created 응답을 반환한다.")
    @Test
    void shouldReturn201_whenSignupSuccess() throws Exception {
      SignupRequest request = userTestHelper.createSignupRequest();

      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.email").value("test@example.com"))
          .andExpect(jsonPath("$.data.nickname").value("testNickname"));
    }

    @DisplayName("이메일 중복 시 409 Conflict 응답을 반환한다")
    @Test
    void shouldReturnConflict_whenEmailIsDuplicate() throws Exception {
      // given
      userRepository.save(User.create("test@example.com", "encodedPW", "길똥이"));

      SignupRequest request = userTestHelper.createSignupRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("USER_004"))
          .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @DisplayName("닉네임 중복 시 409 Conflict 응답을 반환한다")
    @Test
    void shouldReturnConflict_whenNicknameIsDuplicate() throws Exception {
      // given
      userRepository.save(User.create("user@example.com", "userpassword", "testNickname"));

      SignupRequest request = userTestHelper.createSignupRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("USER_005"))
          .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }


  }

  @Nested
  @DisplayName("로그인")
  class Login {

    @DisplayName("로그인 성공 시 200 Ok 응답을 반환한다")
    @Test
    void shouldReturn200_whenLoginSuccess() throws Exception {
      SignupRequest signupRequest = userTestHelper.createSignupRequest();
      userService.signup(signupRequest);

      LoginRequest request = userTestHelper.createLoginRequest();

      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.email").value("test@example.com"))
          .andExpect(jsonPath("$.data.nickname").value("testNickname"));

    }

    @DisplayName("잘못된 비밀번호 입력 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenWrongPassword() throws Exception {
      // given
      userRepository.save(User.create("test@example.com", "encodedPW", "길똥이"));

      LoginRequest request = userTestHelper.createLoginRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("USER_003"))
          .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

    }

    @DisplayName("등록되지 않은 이메일 입력 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenWrongEmail() throws Exception {
      // given
      LoginRequest request = userTestHelper.createLoginRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("USER_003"))
          .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

    }

  }


}
