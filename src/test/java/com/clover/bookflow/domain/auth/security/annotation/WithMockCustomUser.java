package com.clover.bookflow.domain.auth.security.annotation;

import com.clover.bookflow.domain.auth.security.factory.WithMockCustomUserSecurityContextFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

  String uuid() default "00000000-0000-0000-0000-000000000000";

  String email() default "test@bookflow.com";

  String password() default "encoded-password"; // 암호화된 상태라고 가정

  String nickname() default "테스트유저";

  String role() default "USER";

  String status() default "ACTIVE"; // MemberStatus

  long id() default 1L;

}