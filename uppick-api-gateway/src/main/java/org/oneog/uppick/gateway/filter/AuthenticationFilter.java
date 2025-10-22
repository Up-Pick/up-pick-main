package org.oneog.uppick.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.oneog.uppick.common.auth.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            String token = jwtUtil.getJwtFromHeader(request);
            if (token != null) {
                try {
                    Claims claims = jwtUtil.getUserInfoFromToken(token);
                    String memberId = claims.getSubject();
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                        .header("X-Authenticated-User-Id", memberId).build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                } catch (Exception e) {
                    log.error("Invalid token", e);
                }
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}