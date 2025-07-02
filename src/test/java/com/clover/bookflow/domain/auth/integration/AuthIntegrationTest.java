package com.clover.bookflow.domain.auth.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiPath;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
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
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  private TestHelper testHelper;
  private AuthTestHelper authTestHelper;

  @BeforeEach
  void clean() {
    refreshTokenRepository.deleteAll();
    memberRepository.deleteAll();

    testHelper = new TestHelper(mockMvc, objectMapper);
    authTestHelper = new AuthTestHelper();
  }

  @Nested
  @DisplayName("회원가입")
  class Signup {

    @DisplayName("회원가입 성공 시 201 Created 응답을 반환한다.")
    @Test
    void shouldReturn201_whenSignupSuccess() throws Exception {
      SignupRequest request = authTestHelper.createSignupRequest();

      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.memberInfoResponse.email").value("test@example.com"))
          .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value("testNickname"));
    }

    @DisplayName("이메일 중복 시 409 Conflict 응답을 반환한다")
    @Test
    void shouldReturnConflict_whenEmailIsDuplicate() throws Exception {
      // given
      memberRepository.save(Member.create("test@example.com", "encodedPW", "길똥이"));

      SignupRequest request = authTestHelper.createSignupRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("MEMBER_004"))
          .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @DisplayName("닉네임 중복 시 409 Conflict 응답을 반환한다")
    @Test
    void shouldReturnConflict_whenNicknameIsDuplicate() throws Exception {
      // given
      memberRepository.save(Member.create("user@example.com", "userpassword", "testNickname"));

      SignupRequest request = authTestHelper.createSignupRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/signup", request)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("MEMBER_005"))
          .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }


  }

  @Nested
  @DisplayName("로그인")
  class Login {

    @DisplayName("로그인 성공 시 200 Ok 응답을 반환한다")
    @Test
    void shouldReturn200_whenLoginSuccess() throws Exception {
      SignupRequest signupRequest = authTestHelper.createSignupRequest();
      memberService.signup(signupRequest);

      LoginRequest request = authTestHelper.createLoginRequest();

      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.memberInfoResponse.email").value("test@example.com"))
          .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value("testNickname"));

    }

    @DisplayName("잘못된 비밀번호 입력 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenWrongPassword() throws Exception {
      // given
      memberRepository.save(Member.create("test@example.com", "encodedPW", "길똥이"));

      LoginRequest request = authTestHelper.createLoginRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("MEMBER_003"))
          .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

    }

    @DisplayName("등록되지 않은 이메일 입력 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenWrongEmail() throws Exception {
      // given
      LoginRequest request = authTestHelper.createLoginRequest();

      // when & then
      testHelper.postRequest(BASE_URL + "/login", request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("MEMBER_003"))
          .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

    }

  }


}
