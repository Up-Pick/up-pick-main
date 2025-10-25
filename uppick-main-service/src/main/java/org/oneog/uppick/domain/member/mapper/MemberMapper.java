package org.oneog.uppick.domain.member.mapper;

import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.entity.SellDetail;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
	public Member toEntity(SignupRequest request, String encodedPassword) {
		return Member.builder()
			.email(request.getEmail())
			.nickname(request.getNickname())
			.password(encodedPassword)
			.build();
	}

	public PurchaseDetail purchaseDetailToEntity(Long auctionId, Long buyerId, Long productId, Long price) {
		return PurchaseDetail.builder()
			.auctionId(auctionId)
			.buyerId(buyerId)
			.productId(productId)
			.purchasePrice(price)
			.build();
	}

	public SellDetail sellDetailToEntity(Long auctionId, Long sellerId, Long productId, Long price) {
		return SellDetail.builder()
			.auctionId(auctionId)
			.sellerId(sellerId)
			.productId(productId)
			.finalPrice(price)
			.build();
	}
}
