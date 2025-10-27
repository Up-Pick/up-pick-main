package org.oneog.uppick.support.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAuthMemberFactory.class)
public @interface WithMockAuthMember{
    long memberId() default 1L;

    String memberNickname() default "mockUser";
}
