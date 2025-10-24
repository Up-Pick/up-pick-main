package org.oneog.uppick.product.domain.member.service;

import java.util.List;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.product.dto.request.ProductPurchaseInfoWithoutBuyerRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductPurchaseInfoWithBuyerResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetProductPurchaseInfoWithBuyer {

	private final MemberClient memberClient;

	public List<ProductPurchaseInfoWithBuyerResponse> execute(List<ProductPurchaseInfoWithoutBuyerRequest> requests,
		Long memberId) {
		return memberClient.getProductPurchaseInfoWithBuyer(requests, memberId);
	}
}
