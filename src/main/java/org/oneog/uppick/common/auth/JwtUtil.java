package org.oneog.uppick.common.auth;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

	@Value("${jwt.secret.key}")
	private String secretKey;
	private SecretKey key;

	@PostConstruct
	public void init() {
		byte[] bytes = Base64.getDecoder().decode(secretKey);
		key = Keys.hmacShaKeyFor(bytes);
	}

	// 테스트용 setter
	public void setSecretKeyForTest(String secretKey) {
		this.secretKey = secretKey;
		init();
	}

	// 토큰 생성
	public String createToken(Long memberId, String memberNickname) {
		Date date = new Date();

		return BEARER_PREFIX +
			Jwts.builder()
				.subject(String.valueOf(memberId))
				.claim("memberNickname", memberNickname)
				.expiration(new Date(date.getTime() + TOKEN_TIME))
				.issuedAt(date) // 발급일
				.signWith(key, Jwts.SIG.HS256)// 암호화 알고리즘
				.compact();
	}

	// header 에서 JWT 가져오기
	public String getJwtFromHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	public Claims getUserInfoFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}
}

