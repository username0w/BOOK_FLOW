package com.clover.bookflow.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.clover.bookflow.domain.auth.AuthTestHelper;
import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private MemberService memberService;

  private AuthTestHelper authTestHelper;

  // 필드 주입에서 생성자 주입으로 변경으로 인한 수정
  @BeforeEach
  void setUp() {
    memberService = new MemberService(memberRepository, passwordEncoder);
    authTestHelper = new AuthTestHelper();
  }

  @Nested
  @DisplayName("회원 가입")
  class signup {

    @DisplayName("회원 가입 성공 테스트")
    @Test
    void shouldSaveMember_whenSignupCredentialsAreValid() {
      // given
      SignupRequest request = authTestHelper.createSignupRequest();
      String encodedPW = "encodedPW";

      given(memberRepository.existsByEmail(request.email())).willReturn(false);
      given(passwordEncoder.encode(request.password())).willReturn(encodedPW);

      ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

      Member expectedMember = new Member(request.email(), encodedPW, request.nickname());
      // id 없이 테스트 가능
      given(memberRepository.save(any(Member.class))).willReturn(expectedMember);

      // when
      Member savedMember = memberService.signup(request);

      // then
      // 결과 검증
      assertThat(savedMember.getNickname()).isEqualTo(request.nickname());
      assertThat(savedMember.getEmail()).isEqualTo(request.email());

      // 내부 호출 검증
      verify(memberRepository).existsByEmail(request.email());
      verify(passwordEncoder).encode(request.password());
      verify(memberRepository).save(memberCaptor.capture());

      // 캡쳐된 인자 값 검증
      Member capturedMember = memberCaptor.getValue();
      assertThat(capturedMember.getEmail()).isEqualTo(expectedMember.getEmail());
      assertThat(capturedMember.getPassword()).isEqualTo(encodedPW);
      assertThat(capturedMember.getNickname()).isEqualTo(expectedMember.getNickname());
    }

    // 이메일 중복시 회원 가입 실패
    @DisplayName("이미 가입된 이메일로 회원 가입하면 예외가 발생한다")
    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
      // given
      String existingEmail = "hkd111@example.com";
      SignupRequest signupRequest = authTestHelper.createInvalidSignupRequest(existingEmail,
          null, null);
      given(memberRepository.existsByEmail(existingEmail)).willReturn(true);

      // when
      DuplicateResourceException e = assertThrows(DuplicateResourceException.class,
          () -> memberService.signup(signupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 등록된 이메일입니다.");
    }

    // 닉네임 중복 시 회원 가입 실패
    @DisplayName("이미 존재하는 닉네임으로 회원 가입 시 예외가 발생한다")
    @Test
    void shouldThrowException_WhenNicknameAlreadyExists() {
      // given
      String duplicatedNickname = "길똥이";
      SignupRequest signupRequest = authTestHelper.createInvalidSignupRequest(null, null,
          duplicatedNickname);
      given(memberRepository.existsByNickname(duplicatedNickname)).willReturn(true);

      // when
      DuplicateResourceException e = assertThrows(DuplicateResourceException.class,
          () -> memberService.signup(signupRequest));

      // then
      assertThat(e.getMessage()).isEqualTo("이미 존재하는 닉네임입니다.");
    }
  }


}
