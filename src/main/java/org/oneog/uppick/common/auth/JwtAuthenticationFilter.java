package org.oneog.uppick.common.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {

		// HTTP 요청 헤더에서 "Authorization" 헤더값을 가져옴
		String token = jwtUtil.getJwtFromHeader(request);

		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		// JWT 검증 및 인증 설정
		if (!processAuthentication(token, request, response)) {
			return;
		}

		// 4. 인증에 성공하면 다음 필터로 요청을 전달합니다.
		filterChain.doFilter(request, response);
	}

	// JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 메서드
	private boolean processAuthentication(String token, HttpServletRequest request, HttpServletResponse response) throws
		IOException {
		try {

			Claims userInfo = jwtUtil.getUserInfoFromToken(token);

			// SecurityContext에 인증 정보가 없으면 설정 (이미 인증된 경우 중복 설정 방지)
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				setAuthentication(userInfo);
			}
			return true; // 인증 성공

		} catch (SecurityException | MalformedJwtException e) {
			log.error("유효하지 않은 JWT 서명입니다. URI: {}", request.getRequestURI(), e);
			sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.error("만료된 JWT 토큰입니다. URI: {}", request.getRequestURI(), e);
			sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT 토큰입니다. URI: {}", request.getRequestURI(), e);
			sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.error("JWT 클레임이 비어있습니다. URI: {}", request.getRequestURI(), e);
			sendErrorResponse(response, HttpStatus.BAD_REQUEST, "JWT 클레임이 비어있습니다.");
		}
		return false; // 인증 실패
	}

	// JWT Claims에서 사용자 정보를 추출하여 Spring Security의 인증 정보 설정
	private void setAuthentication(Claims claims) {
		// JWT의 subject claim에서 사용자 ID 추출
		Long memberId = Long.valueOf(claims.getSubject());
		// 커스텀 claim에서 닉네임 정보 추출
		String memberNickname = claims.get("memberNickname", String.class);

		AuthMember authMember = new AuthMember(memberId, memberNickname);

		Authentication authenticationToken = new JwtAuthenticationToken(authMember);
		// SecurityContext에 인증 정보 저장 - 이후 @AuthenticationPrincipal로 접근 가능
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	/**
	 * 클라이언트에게 JSON 형식의 에러 응답을 전송합니다.
	 */
	private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json;charset=UTF-8");
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", status.name());
		errorResponse.put("code", status.value());
		errorResponse.put("message", message);
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}