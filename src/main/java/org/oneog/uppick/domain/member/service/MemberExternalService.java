package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.mapper.MemberMapper;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberExternalService implements MemberExternalServiceApi {
	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;

	@Override
	public boolean existsByEmail(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}

	@Override
	public void createUser(SignupRequest request, String encodedPassword) {
		// 회원 엔티티 생성
		Member member = memberMapper.toEntity(request, encodedPassword);

		memberRepository.save(member);
	}

	@Override
	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
	}
}

