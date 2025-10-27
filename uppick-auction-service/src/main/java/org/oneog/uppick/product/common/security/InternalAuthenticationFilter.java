package org.oneog.uppick.product.common.security;

import java.io.IOException;
import java.util.Objects;

import org.oneog.uppick.common.auth.EmptyAuthenticationToken;
import org.oneog.uppick.common.constants.AuthConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InternalAuthenticationFilter extends OncePerRequestFilter {
    private final String internalApiSecret;

    public InternalAuthenticationFilter(@Value("${internal.api.secret}") String internalApiSecret) {
        this.internalApiSecret = internalApiSecret;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain)
        throws ServletException,
        IOException {
        String internalAuthHeaderValue = request.getHeader(AuthConstant.INTERNAL_AUTH_HEADER);
        if (Objects.equals(internalApiSecret, internalAuthHeaderValue)) {
            EmptyAuthenticationToken authentication = new EmptyAuthenticationToken();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Missing authentication headers");
        }
        filterChain.doFilter(request, response);
    }
}
