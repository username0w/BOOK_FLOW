package com.clover.bookflow.domain.auth.security;

import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.enums.MemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomMemberDetails implements UserDetails {

  @Getter
  private final Long id;
  @Getter
  private final UUID uuid;
  private final String email;
  private final String password;
  private final List<GrantedAuthority> authorities;
  private final MemberStatus memberStatus;

  public CustomMemberDetails(Member member) {
    this.id = member.getId();
    this.uuid = member.getUuid();
    this.email = member.getEmail();
    this.password = member.getPassword();  // 암호화된 상태
    this.memberStatus = member.getMemberStatus();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    // 현재 부여된 Role 이 하나여도 Collection 반환이 인터페이스 강제
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }
  // Member 에는 문자열 형태
  // CustomMemberDetails 에는 이미 변환된 형태로 저장

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return memberStatus != MemberStatus.SUSPENDED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return memberStatus == MemberStatus.ACTIVE;
  }

  // Todo: 권한 변환 작업

}
