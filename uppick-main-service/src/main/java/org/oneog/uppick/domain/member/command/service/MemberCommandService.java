package org.oneog.uppick.domain.member.command.service;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.model.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.command.model.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.command.repository.MemberRepository;
import org.oneog.uppick.domain.member.common.exception.MemberErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandService {

	private final MemberRepository memberRepository;

	@Transactional
	public CreditChargeResponse chargeCredit(CreditChargeRequest creditChargeRequest, AuthMember authMember) {

		log.info("MemberCommandService - 크레딧 충전 시도 ⏳");

		Member member = findMemberByIdOrElseThrow(authMember.getMemberId());
		member.addCredit(creditChargeRequest.getAmount());

		CreditChargeResponse response = new CreditChargeResponse(member.getCredit());

		log.info("MemberCommandService - 크레딧 충전 성공 ✅");

		return response;
	}

	private Member findMemberByIdOrElseThrow(Long memberId) {

		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
	}

}
