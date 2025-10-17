package org.oneog.uppick.domain.auth.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.service.MemberExternalService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final MemberExternalService memberExternalService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signup(SignupRequest signupRequest) {

		// 이메일 중복 체크
		if (memberExternalService.existsByEmail(signupRequest.getEmail())) {
			throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
		}

		// 닉네임 중복 체크
		if (memberExternalService.existsByNickname(signupRequest.getNickname())) {
			throw new BusinessException(AuthErrorCode.DUPLICATE_NICKNAME);
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

		memberExternalService.createUser(
			signupRequest.getEmail(),
			signupRequest.getNickname(),
			encodedPassword
		);
	}
}
