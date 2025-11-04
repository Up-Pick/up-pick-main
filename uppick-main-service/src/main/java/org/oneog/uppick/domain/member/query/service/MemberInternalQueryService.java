package org.oneog.uppick.domain.member.query.service;

import java.util.List;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.repository.MemberRepository;
import org.oneog.uppick.domain.member.common.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.query.model.dto.response.PurchasedProductBuyAtResponse;
import org.oneog.uppick.domain.member.query.model.dto.response.SoldProductSellAtResponse;
import org.oneog.uppick.domain.member.query.repository.MemberQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInternalQueryService {

	private final MemberRepository memberRepository;
	private final MemberQueryRepository memberQueryRepository;

	public String getMemberNicknameByMemberId(Long memberId) {

		log.info("MemberInternalQueryService - 닉네임 조회 시도 ⏳");

		Member member = findMemberByIdOrElseThrow(memberId);
		String nickname = member.getNickname();

		log.info("MemberInternalQueryService - 닉네임 조회 성공 ✅");

		return nickname;
	}

	public List<SoldProductSellAtResponse> findSellAtByProductIds(List<Long> productIds) {

		log.info("MemberInternalQueryService - sellAt 조회 시도 ⏳");

		List<SoldProductSellAtResponse> responses = memberQueryRepository.findSellAtByProductIds(productIds);

		log.info("MemberInternalQueryService - sellAt 조회 성공 ✅");

		return responses;
	}

	public List<PurchasedProductBuyAtResponse> findBuyAtByProductIds(List<Long> productIds) {

		log.info("MemberInternalQueryService - buyAt 조회 시도 ⏳");

		List<PurchasedProductBuyAtResponse> responses = memberQueryRepository.findBuyAtByProductIds(productIds);

		log.info("MemberInternalQueryService - buyAt 조회 성공 ✅");

		return responses;
	}

	public long getMemberCreditByMemberId(long memberId) {

		log.info("MemberInternalQueryService - 크레딧 조회 시도 ⏳");

		Member member = findMemberByIdOrElseThrow(memberId);
		long credit = member.getCredit();

		log.info("MemberInternalQueryService - 크레딧 조회 성공 ✅");

		return credit;
	}

	private Member findMemberByIdOrElseThrow(Long memberId) {

		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
	}

}
