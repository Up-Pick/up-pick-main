package org.oneog.uppick.domain.member.service;

import java.util.List;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.domain.member.dto.response.PurchasedProductBuyAtResponse;
import org.oneog.uppick.domain.member.dto.response.SoldProductSellAtResponse;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.entity.SellDetail;
import org.oneog.uppick.domain.member.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.mapper.MemberMapper;
import org.oneog.uppick.domain.member.repository.MemberQueryRepository;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.oneog.uppick.domain.member.repository.PurchaseDetailRepository;
import org.oneog.uppick.domain.member.repository.SellDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInternalService {

	private final MemberRepository memberRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final MemberMapper memberMapper;
	private final PurchaseDetailRepository purchaseDetailRepository;
	private final SellDetailRepository sellDetailRepository;

	public String getMemberNicknameByMemberId(Long memberId) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		return member.getNickname();
	}

	public List<SoldProductSellAtResponse> findSellAtByProductIds(List<Long> productIds) {

		return memberQueryRepository.findSellAtByProductIds(productIds);
	}

	public List<PurchasedProductBuyAtResponse> findBuyAtByProductIds(List<Long> productIds) {

		return memberQueryRepository.findBuyAtByProductIds(productIds);
	}

	public long getMemberCreditByMemberId(long memberId) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		return member.getCredit();
	}

	@Transactional
	public void updateMemberCredit(long memberId, long amount) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.calculateCredit(amount);
	}

	public void registerPurchaseDetail(RegisterPurchaseDetailRequest request) {

		PurchaseDetail purchaseDetail = memberMapper.purchaseDetailToEntity(request);
		purchaseDetailRepository.save(purchaseDetail);
	}

	public void registerSellDetail(RegisterSellDetailRequest request) {

		SellDetail sellDetail = memberMapper.sellDetailToEntity(request);
		sellDetailRepository.save(sellDetail);
	}

}
