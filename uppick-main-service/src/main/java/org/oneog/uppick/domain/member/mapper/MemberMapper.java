package org.oneog.uppick.domain.member.mapper;

import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.dto.request.RegisterSellDetailRequest;
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

	public PurchaseDetail purchaseDetailToEntity(RegisterPurchaseDetailRequest request) {

		return PurchaseDetail.builder()
			.auctionId(request.getAuctionId())
			.buyerId(request.getBuyerId())
			.productId(request.getProductId())
			.purchasePrice(request.getPrice())
			.build();
	}

	public SellDetail sellDetailToEntity(RegisterSellDetailRequest request) {

		return SellDetail.builder()
			.auctionId(request.getAuctionId())
			.sellerId(request.getSellerId())
			.productId(request.getProductId())
			.finalPrice(request.getPrice())
			.build();
	}

}
