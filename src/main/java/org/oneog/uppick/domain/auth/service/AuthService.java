package org.oneog.uppick.domain.auth.service;

import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.entity.Member;
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
	private final JwtUtil jwtUtil;

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

		memberExternalService.createUser(signupRequest, encodedPassword);
	}

	public LoginResponse login(LoginRequest loginRequest) {
		Member member = memberExternalService.findByEmail(loginRequest.getEmail());

		if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
			throw new BusinessException(AuthErrorCode.INVALID_PASSWORD);
		}

		// 3. JWT 생성
		String token = jwtUtil.createToken(member.getId(), member.getNickname());

		return new LoginResponse(token);

	}
}
