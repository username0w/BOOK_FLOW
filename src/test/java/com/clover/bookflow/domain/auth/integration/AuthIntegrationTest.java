package com.clover.bookflow.domain.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clover.bookflow.common.ApiPath;
import com.clover.bookflow.common.TestHelper;
import com.clover.bookflow.config.AbstractIntegrationTest;
import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.domain.TokenWithMeta;
import com.clover.bookflow.domain.auth.dto.request.LoginRequest;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.auth.security.JwtProvider;
import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import com.clover.bookflow.domain.auth.token.repository.RefreshTokenRepository;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest extends AbstractIntegrationTest {

  private static final String BASE_URL = ApiPath.AUTH;

  @Value("${jwt.secret}")
  private String secretKey;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtProvider jwtProvider;

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @PersistenceContext
  EntityManager em;

  private TestHelper testHelper;
  private AuthTestHelper authTestHelper;

  @BeforeEach
  void clean() {
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
          .andExpect(header().exists(HttpHeaders.SET_COOKIE))
          .andExpect(
              header().stringValues(HttpHeaders.SET_COOKIE, hasItem(startsWith("refreshToken="))))
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.memberInfoResponse.email").value(request.email()))
          .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value(request.nickname()))
          .andExpect(jsonPath("$.data.accessTokenResponse.accessToken").isNotEmpty());
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
          .andExpect(header().exists(HttpHeaders.SET_COOKIE))
          .andExpect(
              header().stringValues(HttpHeaders.SET_COOKIE, hasItem(startsWith("refreshToken="))))
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.memberInfoResponse.email").value(request.email()))
          .andExpect(jsonPath("$.data.memberInfoResponse.nickname").value("testNickname"))
          .andExpect(jsonPath("$.data.accessTokenResponse.accessToken").isNotEmpty());
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

  @Nested
  @DisplayName("토큰 재발급")
  class Refresh {

    @DisplayName("토큰 재발급 성공 시 200 Ok 응답을 반환한다")
    @Test
    void shouldReturn200_whenRefreshTokenSuccess() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      Member savedMember = memberRepository.save(member);

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(savedMember.getUuid());
      String refreshToken = refreshTokenWithMeta.token();
      refreshTokenRepository.save(RefreshToken.create(savedMember, refreshTokenWithMeta));

      testHelper.postRequestWithToken(BASE_URL + "/refresh", refreshToken)
          .andExpect(status().isOk())
          .andExpect(header().exists(HttpHeaders.SET_COOKIE))
          .andExpect(
              header().stringValues(HttpHeaders.SET_COOKIE,
                  hasItem(startsWith("refreshToken="))))
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

    }

    @DisplayName("DB 에 없는 토큰으로 재발급 요청 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnNotFound_whenTokenNotFound() throws Exception {
      UUID nonexistentUserId = UUID.randomUUID();

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(nonexistentUserId);
      String refreshToken = refreshTokenWithMeta.token();
      // 토큰 생성 후 save 안함.

      testHelper.postRequestWithToken(BASE_URL + "/refresh", refreshToken)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("TOKEN_002"))
          .andExpect(jsonPath("$.message").value("토큰을 찾을 수 없습니다."));

    }

    @DisplayName("만료된 토큰으로 재발급 요청 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenTokenExpired() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      memberRepository.save(member);
      String jti = UUID.randomUUID().toString();
      Date now = new Date(System.currentTimeMillis());
      Date validity = new Date(System.currentTimeMillis() - 60000);
      Instant expiresAt = validity.toInstant();

      String expiredRefreshToken = Jwts.builder()
          .setSubject(member.getUuid().toString()) // 토큰에 사용할 식별자 값 저장
          .setId(jti)
          .setIssuedAt(now)
          .setExpiration(validity)
          .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
          .compact();

      refreshTokenRepository.save(
          RefreshToken.create(member, TokenWithMeta.of(expiredRefreshToken, jti, expiresAt)));

      testHelper.postRequestWithToken(BASE_URL + "/refresh", expiredRefreshToken)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("TOKEN_001"))
          .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."));
    }

    @DisplayName("탈퇴한 회원으로 재발급 요청 시 404 Not Found 응답을 반환한다")
    @Test
    void shouldReturnNotFound_whenMemberNotFound() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      member.withdraw();
      Member savedMember = memberRepository.save(member);

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(savedMember.getUuid());
      String refreshToken = refreshTokenWithMeta.token();

      refreshTokenRepository.save(
          RefreshToken.create(savedMember, refreshTokenWithMeta)
      );
      log.info("member ID " + savedMember.getId());

      testHelper.postRequestWithToken(BASE_URL + "/refresh", refreshToken)
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("MEMBER_001"))
          .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

  }

  @Nested
  @DisplayName("로그아웃")
  class Logout {

    @DisplayName("로그아웃 성공 시 200 Ok 응답을 반환한다")
    @Test
    void shouldReturn200_whenRefreshTokenSuccess() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      Member savedMember = memberRepository.save(member);

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(savedMember.getUuid());
      String refreshToken = refreshTokenWithMeta.token();
      refreshTokenRepository.save(RefreshToken.create(savedMember, refreshTokenWithMeta));

      testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
          .andExpect(status().isOk())
          .andExpect(header().exists(HttpHeaders.SET_COOKIE))
          .andExpect(
              header().string(HttpHeaders.SET_COOKIE,
                  allOf(
                      containsString("refreshToken=;"),
                      containsString("Max-Age=0")
                  )
              ))
          .andExpect(jsonPath("$.success").value(true));

      boolean tokenExists = refreshTokenRepository.existsByJti(refreshTokenWithMeta.jti());
      assertThat(tokenExists).isFalse();
    }

    @DisplayName("DB 에 존재하지 않는 토큰으로 로그아웃 시 401 Unauthorized 반환")
    @Test
    void shouldReturn401_whenInvalidTokenLogout() throws Exception {
      UUID testUserId = UUID.randomUUID();

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(testUserId);
      String invalidToken = refreshTokenWithMeta.token();

      testHelper.postRequestWithToken(BASE_URL + "/logout", invalidToken)
          .andExpect(status().isUnauthorized());
    }

    @DisplayName("이미 삭제된 토큰으로 로그아웃 시도 시 401 반환")
    @Test
    void shouldReturn401_whenTokenAlreadyDeleted() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      Member savedMember = memberRepository.save(member);

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(savedMember.getUuid());
      String refreshToken = refreshTokenWithMeta.token();

      // 토큰을 저장하지 않고 바로 로그아웃 시도
      testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
          .andExpect(status().isUnauthorized());
    }

    @DisplayName("로그아웃 후 동일 토큰으로 재요청 시 401 Unauthorized")
    @Test
    void shouldRejectReuseTokenAfterLogout() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      Member savedMember = memberRepository.save(member);

      TokenWithMeta refreshTokenWithMeta = jwtProvider.createRefreshToken(savedMember.getUuid());
      String refreshToken = refreshTokenWithMeta.token();
      refreshTokenRepository.save(RefreshToken.create(savedMember, refreshTokenWithMeta));

      testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
          .andExpect(status().isOk());

      testHelper.postRequestWithToken(BASE_URL + "/logout", refreshToken)
          .andExpect(status().isUnauthorized());
    }

    @DisplayName("만료된 토큰으로 로그아웃 요청 시 401 Unauthorized 응답을 반환한다")
    @Test
    void shouldReturnUnauthorized_whenTokenExpired() throws Exception {
      Member member = MemberTestHelper.createTestUser();
      memberRepository.save(member);
      String jti = UUID.randomUUID().toString();
      Date now = new Date(System.currentTimeMillis());
      Date validity = new Date(System.currentTimeMillis() - 60000);
      Instant expiresAt = validity.toInstant();

      String expiredRefreshToken = Jwts.builder()
          .setSubject(member.getUuid().toString()) // 토큰에 사용할 식별자 값 저장
          .setId(jti)
          .setIssuedAt(now)
          .setExpiration(validity)
          .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
          .compact();

      refreshTokenRepository.save(
          RefreshToken.create(member, TokenWithMeta.of(expiredRefreshToken, jti, expiresAt)));

      testHelper.postRequestWithToken(BASE_URL + "/logout", expiredRefreshToken)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.code").value("TOKEN_001"))
          .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."));
    }

  }


}
