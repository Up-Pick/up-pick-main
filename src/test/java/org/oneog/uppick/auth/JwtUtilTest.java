package org.oneog.uppick.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.auth.JwtUtil;

import io.jsonwebtoken.Claims;

public class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		// 테스트용 secretKey (Base64 인코딩)
		jwtUtil.setSecretKeyForTest("bXlTdXBlclNlY3JldEtleU15U2VjcmV0S2V5MTIzNDU2");
	}

	@Test
	void testTokenCreationAndValidation() {
		Long memberId = 1L;
		String memberNickname = "testUser";

		// 토큰 생성
		String tokenWithBearer = jwtUtil.createToken(memberId, memberNickname);
		assertNotNull(tokenWithBearer);
		assertTrue(tokenWithBearer.startsWith("Bearer "));

		// Bearer 제거 후 Claims 확인
		String pureToken = tokenWithBearer.substring(7);
		Claims claims = jwtUtil.getUserInfoFromToken(pureToken);

		assertEquals(memberId.toString(), claims.getSubject());
		assertEquals(memberNickname, claims.get("memberNickname", String.class));
	}
}