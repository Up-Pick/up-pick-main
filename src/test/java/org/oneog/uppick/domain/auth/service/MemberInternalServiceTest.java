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
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class) // JUnit5에서 Mockito를 사용하기 위한 확장 기능
class AuthServiceTest {

	@Mock // 가짜(Mock) MemberRepository 객체 생성
	private MemberRepository memberRepository;

	@Mock // 가짜(Mock) PasswordEncoder 객체 생성
	private PasswordEncoder passwordEncoder;

	@InjectMocks // @Mock으로 만든 가짜 객체들을 AuthService에 주입
	private AuthService authService;

	@Test
	@DisplayName("signup_성공적인회원가입_예외미발생")
	void signup_정상적인요청_성공() {
		// given (주어진 상황)
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();

		when(memberRepository.existsByEmail(anyString())).thenReturn(false); // 이메일이 중복되지 않음
		when(memberRepository.existsByNickname(anyString())).thenReturn(false); // 닉네임이 중복되지 않음
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); // 패스워드 인코딩 결과

		// when
		// then
		assertDoesNotThrow(() -> authService.signup(request)); // 예외가 발생하지 않아야 함

		// 추가 검증: memberRepository.save가 Member 객체를 인자로 1번 호출되었는지 확인
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("signup_중복된이메일_BusinessException발생")
	void signup_중복된이메일_실패() {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();

		when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true); // 이메일이 이미 존재함

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.signup(request);
		});

		assertEquals(AuthErrorCode.DUPLICATE_EMAIL, exception.getErrorCode()); // 발생한 예외 코드가 DUPLICATE_EMAIL인지 확인
		verify(memberRepository, never()).save(any(Member.class)); // save 메소드가 절대 호출되지 않았는지 확인
	}

	@Test
	@DisplayName("signup_중복된닉네임_BusinessException발생")
	void signup_중복된닉네임_실패() {
		// given
		SignupRequest request = SignupRequest.builder()
			.email("test@email.com")
			.nickname("테스트유저")
			.password("Password123!")
			.build();
		when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false); // 이메일은 중복되지 않음
		when(memberRepository.existsByNickname(request.getNickname())).thenReturn(true); // 닉네임이 이미 존재함

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			authService.signup(request);
		});

		assertEquals(AuthErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode()); // 발생한 예외 코드가 DUPLICATE_NICKNAME인지 확인
		verify(memberRepository, never()).save(any(Member.class)); // save 메소드가 절대 호출되지 않았는지 확인
	}
}