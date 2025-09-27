package com.clover.bookflow.domain.auth.security.permission;

import java.io.Serializable;
import org.springframework.security.core.Authentication;

public interface DomainPermissionEvaluator {

  boolean supports(String targetType); // 지원하는 도메인인지 확인

  boolean hasPermission(Authentication authentication, Serializable targetId, String permission);
}
