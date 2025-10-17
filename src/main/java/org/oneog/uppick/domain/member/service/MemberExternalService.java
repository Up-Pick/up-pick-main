package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberExternalService implements MemberExternalServiceApi {
	private final MemberRepository memberRepository;

	@Override
	public boolean existsByEmail(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}

	@Override
	public void createUser(Member member) {
		memberRepository.save(member);
	}

}
