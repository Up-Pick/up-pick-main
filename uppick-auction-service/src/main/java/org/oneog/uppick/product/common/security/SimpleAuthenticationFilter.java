package org.oneog.uppick.product.common.security;

import java.io.IOException;

import org.oneog.uppick.common.auth.SimpleAuthenticationToken;
import org.oneog.uppick.common.constants.AuthConstant;
import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String memberIdStr = request.getHeader(AuthConstant.AUTH_MEMBER_ID_HEADER);
		String memberNickname = request.getHeader(AuthConstant.AUTH_MEMBER_NICKNAME_HEADER);

		if (memberIdStr != null && memberNickname != null) {
			try {
				long memberId = Long.parseLong(memberIdStr);
				AuthMember authMember = new AuthMember(memberId, memberNickname);
				SimpleAuthenticationToken authentication = new SimpleAuthenticationToken(authMember);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (NumberFormatException e) {
				log.warn("Invalid memberId format: {}", memberIdStr);
			}
		} else {
			log.warn("Missing authentication headers");
		}

		filterChain.doFilter(request, response);
	}

}
