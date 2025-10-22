package org.oneog.uppick.product.support.auth;

import org.oneog.uppick.common.auth.SimpleAuthenticationToken;
import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockAuthMemberFactory implements WithSecurityContextFactory<WithMockAuthMember> {
    @Override
    public SecurityContext createSecurityContext(
        WithMockAuthMember annotation) {
        Long memberId = annotation.memberId();
        String memberNickname = annotation.memberNickname();

        AuthMember authMember = new AuthMember(memberId, memberNickname);
        SimpleAuthenticationToken authentication = new SimpleAuthenticationToken(authMember);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}