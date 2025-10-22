package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.entity.SellDetail;
import org.oneog.uppick.domain.member.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.mapper.MemberMapper;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.oneog.uppick.domain.member.repository.PurchaseDetailRepository;
import org.oneog.uppick.domain.member.repository.SellDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberExternalService implements MemberExternalServiceApi {
	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;
	private final PurchaseDetailRepository purchaseDetailRepository;
	private final SellDetailRepository sellDetailRepository;

	@Override
	public void registerPurchaseDetail(Long auctionId, Long buyerId, Long productId, Long price) {
		PurchaseDetail purchaseDetail = memberMapper.purchaseDetailToEntity(auctionId, buyerId, productId, price);
		purchaseDetailRepository.save(purchaseDetail);
	}

	@Override
	public void registerSellDetail(Long auctionId, Long sellerId, Long productId, Long price) {
		SellDetail sellDetail = memberMapper.sellDetailToEntity(auctionId, sellerId, productId, price);
		sellDetailRepository.save(sellDetail);
	}

	@Override
	public boolean existsByEmail(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}

	@Override
	public void createUser(SignupRequest request, String encodedPassword) {
		// 회원 엔티티 생성
		Member member = memberMapper.toEntity(request, encodedPassword);

		memberRepository.save(member);
	}

	@Override
	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	@Override
	public void updateMemberCredit(Long memberId, Long amount) {
		Member member = memberRepository.findMemberById(memberId);

		long updatedCredit = member.getCredit() + amount;
		if (updatedCredit < 0) {
			throw new BusinessException(MemberErrorCode.INVALID_CHARGE_AMOUNT);
		}

		member.updateCredit(updatedCredit);
	}
}
