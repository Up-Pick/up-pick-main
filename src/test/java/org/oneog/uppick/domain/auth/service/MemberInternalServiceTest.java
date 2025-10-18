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
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.service.MemberExternalService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberExternalService memberExternalService;

	@Mock
	private PasswordEncoder passwordEncoder;

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
			request.getEmail(),
			request.getNickname(),
			encodedPassword
		);
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
		verify(memberExternalService, never()).createUser(anyString(), anyString(), anyString());
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
		verify(memberExternalService, never()).createUser(anyString(), anyString(), anyString());
	}
}