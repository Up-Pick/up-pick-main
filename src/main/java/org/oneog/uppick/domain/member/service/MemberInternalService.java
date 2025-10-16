package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.domain.member.repository.MemberQueryRepository;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberInternalService {
	private final MemberRepository memberRepository;
	private final MemberQueryRepository memberQueryRepository;
}
