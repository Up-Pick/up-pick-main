package org.oneog.uppick.gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.common.constants.AuthConstant;
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
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            String token = jwtUtil.getJwtFromHeader(request);
            if (token != null) {
                try {
                    Claims claims = jwtUtil.getUserInfoFromToken(token);
                    String memberId = claims.getSubject();
                    String memberNickname = (String)claims.get("memberNickname");
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                        .header(AuthConstant.AUTH_MEMBER_ID, memberId)
                        .header(AuthConstant.AUTH_MEMBER_NICKNAME, memberNickname).build();
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