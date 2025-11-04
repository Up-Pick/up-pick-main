package org.oneog.uppick.domain.member.common.mapper;

import org.oneog.uppick.domain.auth.command.model.dto.request.SignupRequest;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.command.entity.SellDetail;
import org.oneog.uppick.domain.member.command.model.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.command.model.dto.request.RegisterSellDetailRequest;
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
