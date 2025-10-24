package org.oneog.uppick.common.security;

import java.io.IOException;

import org.oneog.uppick.common.auth.EmptyAuthenticationToken;
import org.oneog.uppick.common.constants.AuthConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.common.base.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InternalAuthenticationFilter extends OncePerRequestFilter {
    private final String internalApiSecret;

    public InternalAuthenticationFilter(@Value("${internal.api.secret}")
    String internalApiSecret) {
        this.internalApiSecret = internalApiSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String internalAuthHeaderValue = request.getHeader(AuthConstant.INTERNAL_AUTH_HEADER);
        if (Objects.equal(internalApiSecret, internalAuthHeaderValue)) {
            EmptyAuthenticationToken authentication = new EmptyAuthenticationToken();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Missing authentication headers");
        }
        filterChain.doFilter(request, response);
    }
}
