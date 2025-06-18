package com.clover.bookflow.domain.member.service;

import com.clover.bookflow.domain.auth.dto.request.SignupRequest;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import com.clover.bookflow.global.errorcode.MemberErrorCode;
import com.clover.bookflow.global.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor // 생성자 주입이 더 안전, 깔끔
public class MemberService {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Member signup(SignupRequest request) {
    log.info("회원가입 시도: {}", request);

    // 입력값 자체 검사는 커스텀 어노테이션
    // db 조회 필요한 검사는 서비스에서 처리

    // 이메일, 닉네임 중복 체크 (비즈니스 유효성 검증)
    validateDuplicateEmail(request.email());
    validateDuplicateNickname(request.nickname());

    String encodedPassword = passwordEncoder.encode(request.password());

    // Member 도메인 객체 생성 (정적 팩토리 메서드 사용)
    Member member = Member.create(request.email(), encodedPassword, request.nickname());

    return memberRepository.save(member);
  }

  private void validateDuplicateEmail(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new DuplicateResourceException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  private void validateDuplicateNickname(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new DuplicateResourceException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
    }
  }


}
