package org.oneog.uppick.common.auth;

import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class SimpleAuthenticationToken extends AbstractAuthenticationToken {
    private final AuthMember authMember;

    public SimpleAuthenticationToken(AuthMember authMember) {
        super(null);
        this.authMember = authMember;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authMember;
    }
}
