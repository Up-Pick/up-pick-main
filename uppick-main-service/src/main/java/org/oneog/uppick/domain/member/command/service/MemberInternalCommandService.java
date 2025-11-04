package org.oneog.uppick.domain.member.command.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.command.entity.SellDetail;
import org.oneog.uppick.domain.member.command.model.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.command.model.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.domain.member.command.repository.MemberRepository;
import org.oneog.uppick.domain.member.command.repository.PurchaseDetailRepository;
import org.oneog.uppick.domain.member.command.repository.SellDetailRepository;
import org.oneog.uppick.domain.member.common.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.common.mapper.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInternalCommandService {

	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;
	private final PurchaseDetailRepository purchaseDetailRepository;
	private final SellDetailRepository sellDetailRepository;

	@Transactional
	public void updateMemberCredit(long memberId, long amount) {

		log.info("MemberInternalCommandService - 크레딧 업데이트 시도 ⏳");

		Member member = findMemberByIdOrElseThrow(memberId);
		member.calculateCredit(amount);

		log.info("MemberInternalCommandService - 크레딧 업데이트 성공 ✅");
	}

	@Transactional
	public void registerPurchaseDetail(RegisterPurchaseDetailRequest request) {

		log.info("MemberInternalCommandService - purchaseDetail 데이터 저장 시도 ⏳");

		PurchaseDetail purchaseDetail = memberMapper.purchaseDetailToEntity(request);
		purchaseDetailRepository.save(purchaseDetail);

		log.info("MemberInternalCommandService - purchaseDetail 데이터 저장 성공 ✅");
	}

	@Transactional
	public void registerSellDetail(RegisterSellDetailRequest request) {

		log.info("MemberInternalCommandService - sellDetail 데이터 저장 시도 ⏳");

		SellDetail sellDetail = memberMapper.sellDetailToEntity(request);
		sellDetailRepository.save(sellDetail);

		log.info("MemberInternalCommandService - sellDetail 데이터 저장 성공 ✅");
	}

	private Member findMemberByIdOrElseThrow(Long memberId) {

		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
	}

}
