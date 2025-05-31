package com.clover.bookflow.domain.auth.service;

import com.clover.bookflow.domain.auth.security.CustomUserDetails;
import com.clover.bookflow.domain.user.entity.User;
import com.clover.bookflow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("해당 이메일 사용자 찾을 수 없음."));

    return new CustomUserDetails(user);
  }
}
