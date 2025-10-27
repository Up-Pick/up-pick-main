package org.oneog.uppick.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.service.DefaultMemberInnerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private DefaultMemberInnerService memberExternalService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원가입 성공")
	void signup_정상적인요청_성공() {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();
		String encodedPassword = "encodedPassword";

		when(memberExternalService.existsByEmail(anyString())).thenReturn(false);
		when(memberExternalService.existsByNickname(anyString())).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

		// when & then
		assertDoesNotThrow(() -> authService.signup(request));

		// memberExternalService.createUser()가 정확한 인자들로 1번 호출되었는지 검증합니다.
		verify(memberExternalService, times(1)).createUser(
			request, // request 객체 전체를 전달
			encodedPassword);
	}

	@Test
	@DisplayName("중복된 이메일로 인한 회원가입 실패")
	void signup_중복된이메일_실패() {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();

		when(memberExternalService.existsByEmail(request.getEmail())).thenReturn(true);

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.signup(request);
		});

		assertEquals(AuthErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());

		// createUser 메서드가 호출되지 않았는지 확인.
		verify(memberExternalService, never()).createUser(any(SignupRequest.class), anyString());
	}

	@Test
	@DisplayName("중복된 닉네임으로 인한 회원가입 실패")
	void signup_중복된닉네임_실패() {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();

		when(memberExternalService.existsByEmail(request.getEmail())).thenReturn(false);
		when(memberExternalService.existsByNickname(request.getNickname())).thenReturn(true);

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.signup(request);
		});

		assertEquals(AuthErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());

		// createUser 메서드가 호출되지 않았는지 확인.
		verify(memberExternalService, never()).createUser(any(SignupRequest.class), anyString());
	}

	@Test
	@DisplayName("로그인 성공")
	void login_성공() {
		// given
		LoginRequest request = new LoginRequest("test@email.com", "password123");
		Member member = Member.builder()
			.email("test@email.com")
			.password("encodedPassword")
			.nickname("테스트유저")
			.build();

		ReflectionTestUtils.setField(member, "id", 1L);

		String expectedToken = "dummy-jwt-token-string";

		when(memberExternalService.findByEmail(request.getEmail())).thenReturn(member);
		when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(true);
		when(jwtUtil.createToken(member.getId(), member.getNickname())).thenReturn(expectedToken);

		// when
		LoginResponse response = authService.login(request);

		// then
		assertNotNull(response);
		assertEquals(expectedToken, response.getAccessToken());
		verify(jwtUtil, times(1)).createToken(1L, "테스트유저");
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 사용자")
	void login_실패_사용자없음() {
		// given
		LoginRequest request = new LoginRequest("wrong@email.com", "password123");

		when(memberExternalService.findByEmail(anyString()))
			.thenThrow(new BusinessException(AuthErrorCode.USER_NOT_FOUND));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.login(request);
		});

		assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
		verify(jwtUtil, never()).createToken(anyLong(), anyString());
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void login_실패_비밀번호불일치() {
		// given
		LoginRequest request = new LoginRequest("test@email.com", "wrong-password");
		Member member = Member.builder()
			.email("test@email.com")
			.password("encodedPassword")
			.build();

		when(memberExternalService.findByEmail(request.getEmail())).thenReturn(member);
		when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(false);

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.login(request);
		});

		assertEquals(AuthErrorCode.INVALID_PASSWORD, exception.getErrorCode());
		verify(jwtUtil, never()).createToken(anyLong(), anyString());
	}
}
