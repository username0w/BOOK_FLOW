package com.clover.bookflow.domain.auth.security;

import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  @Getter
  private final Long id;
  private final String email;
  private final String password;
  private final List<GrantedAuthority> authorities;
  private final UserStatus userStatus;

  public CustomUserDetails(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.password = user.getPassword();  // 암호화된 상태
    this.userStatus = user.getUserStatus();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    // 현재 부여된 Role 이 하나여도 Collection 반환이 인터페이스 강제
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }
  // User 에는 문자열 형태
  // CustomUserDetails 에는 이미 변환된 형태로 저장


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
    return userStatus != UserStatus.SUSPENDED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return userStatus == UserStatus.ACTIVE;
  }

  // Todo: 권한 변환 작업

}
