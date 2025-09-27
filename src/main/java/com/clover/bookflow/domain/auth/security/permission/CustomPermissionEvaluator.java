package com.clover.bookflow.domain.auth.security.permission;

import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

  private final List<DomainPermissionEvaluator> evaluators;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    return false;
  }
  // 객체 직접 넘겨줘야 해서 보통 두번째 메서드 사용


  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (authentication == null || permission == null || targetId == null || targetType == null) {
      return false;
    }

    // 관리자 무조건 통과
    if (authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
      return true;
    }

    return evaluators.stream()
        .filter(e -> e.supports(targetType))
        .findFirst()
        .map(e -> e.hasPermission(authentication, targetId, permission.toString()))
        .orElse(false);
  }
}
