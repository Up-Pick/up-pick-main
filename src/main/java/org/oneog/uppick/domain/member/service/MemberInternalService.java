package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.repository.MemberQueryRepository;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInternalService {
	private final MemberRepository memberRepository;
	private final MemberQueryRepository memberQueryRepository;

	@Transactional
	public CreditChargeResponse chargeCredit(CreditChargeRequest creditChargeRequest, AuthMember authMember) {

		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.addCredit(creditChargeRequest.getAmount());

		//더티 체킹 db 자동 저장

		return new CreditChargeResponse(member.getCredit());
	}
}
