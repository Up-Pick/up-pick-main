package org.oneog.uppick.auction.domain.member.service;

import java.util.List;

import org.oneog.uppick.auction.domain.member.client.MemberClient;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.UpdateMemberCreditRequest;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellAtResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultMemberInnerService implements MemberInnerService {

	private final MemberClient memberClient;

	@Override
	public long getMemberCredit(long memberId) {

		return memberClient.getMemberCredit(memberId);
	}

	@Override
	public String getMemberNickname(long memberId) {

		return memberClient.getMemberNickname(memberId);
	}

	@Override
	public List<ProductBuyAtResponse> getProductBuyAt(List<Long> productIds) {

		return memberClient.getPurchasedProductsBuyAt(productIds);
	}

	@Override
	public List<ProductSellAtResponse> getProductSellAt(List<Long> productIds) {

		return memberClient.getSoldProductsSellAt(productIds);
	}

	@Override
	public void registerPurchaseDetail(RegisterPurchaseDetailRequest request) {

		memberClient.registerPurchaseDetail(request);
	}

	@Override
	public void registerSellDetail(RegisterSellDetailRequest request) {

		memberClient.registerSellDetail(request);
	}

	@Override
	public void updateMemberCredit(long memberId, long credit) {

		UpdateMemberCreditRequest request = new UpdateMemberCreditRequest(credit);
		memberClient.updateMemberCredit(memberId, request);
	}

}
