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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		log.info("MemberInternalService - 닉네임 조회 시도 ⏳");

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		log.info("MemberInternalService - 닉네임 조회 성공 ✅");

		return member.getNickname();
	}

	public List<SoldProductSellAtResponse> findSellAtByProductIds(List<Long> productIds) {

		log.info("MemberInternalService - sellAt 조회 시도 ⏳");

		List<SoldProductSellAtResponse> responses = memberQueryRepository.findSellAtByProductIds(productIds);

		log.info("MemberInternalService - sellAt 조회 성공 ✅");

		return responses;
	}

	public List<PurchasedProductBuyAtResponse> findBuyAtByProductIds(List<Long> productIds) {

		log.info("MemberInternalService - buyAt 조회 시도 ⏳");

		List<PurchasedProductBuyAtResponse> responses = memberQueryRepository.findBuyAtByProductIds(productIds);

		log.info("MemberInternalService - buyAt 조회 성공 ✅");

		return responses;
	}

	public long getMemberCreditByMemberId(long memberId) {

		log.info("MemberInternalService - 크레딧 조회 시도 ⏳");

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		log.info("MemberInternalService - 크레딧 조회 성공 ✅");

		return member.getCredit();
	}

	@Transactional
	public void updateMemberCredit(long memberId, long amount) {

		log.info("MemberInternalService - 크레딧 업데이트 시도 ⏳");

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

		member.calculateCredit(amount);

		log.info("MemberInternalService - 크레딧 업데이트 성공 ✅");
	}

	public void registerPurchaseDetail(RegisterPurchaseDetailRequest request) {

		log.info("MemberInternalService - purchaseDetail 데이터 저장 시도 ⏳");

		PurchaseDetail purchaseDetail = memberMapper.purchaseDetailToEntity(request);
		purchaseDetailRepository.save(purchaseDetail);

		log.info("MemberInternalService - purchaseDetail 데이터 저장 성공 ✅");
	}

	public void registerSellDetail(RegisterSellDetailRequest request) {

		log.info("MemberInternalService - sellDetail 데이터 저장 시도 ⏳");

		SellDetail sellDetail = memberMapper.sellDetailToEntity(request);
		sellDetailRepository.save(sellDetail);

		log.info("MemberInternalService - sellDetail 데이터 저장 성공 ✅");
	}

}
