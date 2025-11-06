package org.oneog.uppick.domain.member.query.service;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.repository.MemberRepository;
import org.oneog.uppick.domain.member.common.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.query.model.dto.response.CreditGetResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

	private final MemberRepository memberRepository;

	public CreditGetResponse getCredit(AuthMember authMember) {

		log.debug("MemberQueryService - 크레딧 조회 시도 ⏳");

		Member member = findMemberByIdOrElseThrow(authMember.getMemberId());
		CreditGetResponse response = new CreditGetResponse(member.getCredit());

		log.debug("MemberQueryService - 크레딧 조회 성공 ✅");

		return response;
	}

	private Member findMemberByIdOrElseThrow(Long memberId) {

		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
	}

}
