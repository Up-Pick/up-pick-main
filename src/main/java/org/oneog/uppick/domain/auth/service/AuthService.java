package org.oneog.uppick.domain.auth.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signup(@Valid SignupRequest signupRequest) {
		
		// 이메일 중복 체크
		if (memberRepository.existsByEmail(signupRequest.getEmail())) {
			throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
		}

		// 닉네임 중복 체크
		if (memberRepository.existsByNickname(signupRequest.getNickname())) {
			throw new BusinessException(AuthErrorCode.DUPLICATE_NICKNAME);
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

		// 회원 엔티티 생성
		Member member = Member.builder()
			.email(signupRequest.getEmail())
			.nickname(signupRequest.getNickname())
			.password(encodedPassword)
			.build();

		memberRepository.save(member);
	}
}
