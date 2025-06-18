package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("해당 이메일 사용자 찾을 수 없음."));

    return new CustomMemberDetails(member);
  }
}
