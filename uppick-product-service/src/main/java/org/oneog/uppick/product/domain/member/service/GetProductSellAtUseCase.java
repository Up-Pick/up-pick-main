package org.oneog.uppick.product.domain.member.service;

import java.util.List;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellAtResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProductSellAtUseCase {

	private final MemberClient memberClient;

	public List<ProductSellAtResponse> execute(List<Long> productIds) {
		return memberClient.getSoldProductsSellAt(productIds);
	}
}
