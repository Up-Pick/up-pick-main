package org.oneog.uppick.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.auth.JwtAuthenticationToken;
import org.oneog.uppick.common.dto.AuthMember;

class JwtAuthenticationTokenTest {
	//getPrincipal() → AuthMember 객체가 제대로 반환되는지
	//
	// getCredentials() → JWT 인증에서는 null 반환 확인
	//
	// isAuthenticated() → 생성자에서 setAuthenticated(true) 했는지 확인
	//
	// 권한 리스트(getAuthorities()) → List.of()로 초기화 됐는지 확인
	@Test
	void testAuthenticationToken() {
		// given
		AuthMember authMember = new AuthMember(1L, "testUser");

		// when
		JwtAuthenticationToken token = new JwtAuthenticationToken(authMember);

		// then
		assertTrue(token.isAuthenticated(), "토큰이 인증된 상태여야 함");
		assertEquals(authMember, token.getPrincipal(), "Principal이 AuthMember여야 함");
		assertNull(token.getCredentials(), "Credentials는 null이어야 함");
		assertNotNull(token.getAuthorities(), "Authorities는 null이 아님");
		assertEquals(0, token.getAuthorities().size(), "권한 리스트는 비어 있어야 함");
	}
}