package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.dto.response.CreditGetResponse;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public CreditChargeResponse chargeCredit(CreditChargeRequest creditChargeRequest, AuthMember authMember) {

		log.info("MemberService.chargeCredit : 크레딧 충전 시도");

		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.addCredit(creditChargeRequest.getAmount());

		log.info("MemberService.chargeCredit : 크레딧 충전 성공 ✅");

		return new CreditChargeResponse(member.getCredit());
	}

	public CreditGetResponse getCredit(AuthMember authMember) {

		log.info("MemberService.getCredit : 크레딧 조회 시도");

		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		log.info("MemberService.getCredit : 크레딧 조회 성공 ✅");

		return new CreditGetResponse(member.getCredit());
	}

}
