package org.oneog.uppick.common.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class EmptyAuthenticationToken extends AbstractAuthenticationToken {
    public EmptyAuthenticationToken() {
        super(null);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
