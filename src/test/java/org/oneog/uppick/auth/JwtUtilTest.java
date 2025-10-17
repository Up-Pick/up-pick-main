package org.oneog.uppick.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.auth.JwtUtil;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;

public class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		// 테스트용 secretKey (Base64 인코딩)
		String testSecretKey = "bXlTdXBlclNlY3JldEtleU15U2VjcmV0S2V5MTIzNDU2";
		
		//ReflectionTestUtils를 사용해 private 필드인 'secretKey'에 값을 직접 주입
		ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecretKey);

		// 2. @PostConstruct가 동작하지 않으므로, 수동으로 초기화 메서드를 호출하여 'key' 필드를 생성합니다.
		jwtUtil.init();
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