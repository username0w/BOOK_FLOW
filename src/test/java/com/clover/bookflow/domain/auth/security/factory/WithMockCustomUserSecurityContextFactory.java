package com.clover.bookflow.domain.auth.security.factory;

import com.clover.bookflow.domain.auth.security.CustomMemberDetails;
import com.clover.bookflow.domain.auth.security.annotation.WithMockCustomUser;
import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.member.entity.MemberTestHelper;
import com.clover.bookflow.domain.member.enums.MemberStatus;
import com.clover.bookflow.domain.member.enums.Role;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements
    WithSecurityContextFactory<WithMockCustomUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
    UUID uuid = UUID.fromString(annotation.uuid());
    String email = annotation.email();
    String password = annotation.password();
    String nickname = annotation.nickname();
    Role role = Role.valueOf(annotation.role());
    MemberStatus status = MemberStatus.valueOf(annotation.status());

    Member member = MemberTestHelper.createTestUser(
        uuid,
        email,
        password,
        nickname,
        role,
        status
    );

    CustomMemberDetails principal = new CustomMemberDetails(member);
    UsernamePasswordAuthenticationToken auth =
        UsernamePasswordAuthenticationToken.authenticated(
            principal,
            principal.getPassword(),
            principal.getAuthorities()
        );

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    return context;
  }
}
