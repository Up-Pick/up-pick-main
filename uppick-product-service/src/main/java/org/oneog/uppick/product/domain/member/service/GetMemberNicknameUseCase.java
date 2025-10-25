package org.oneog.uppick.product.domain.member.service;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMemberNicknameUseCase {

	private final MemberClient memberClient;

	public String execute(Long memberId) {
		return memberClient.getMemberNickname(memberId);
	}
}
