package org.oneog.uppick.domain.auth.command.service;

import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.command.model.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.command.model.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.command.model.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.common.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.service.DefaultMemberInnerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandService {

	private final DefaultMemberInnerService memberExternalService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Transactional
	public void signup(SignupRequest signupRequest) {

		log.debug("AuthCommandService - 회원가입 시도 ⏳");

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

		log.debug("AuthCommandService - 회원가입 성공 ✅");
	}

	public LoginResponse login(LoginRequest loginRequest) {

		log.debug("AuthCommandService - 로그인 시도 ⏳");

		Member member = memberExternalService.findByEmail(loginRequest.getEmail());

		if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
			throw new BusinessException(AuthErrorCode.INVALID_PASSWORD);
		}

		// 3. JWT 생성
		String token = jwtUtil.createToken(member.getId(), member.getNickname());
		LoginResponse response = new LoginResponse(token);

		log.debug("AuthCommandService - 로그인 성공 ✅");

		return response;
	}

}
