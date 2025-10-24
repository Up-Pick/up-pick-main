package org.oneog.uppick.product.domain.member.service;

import java.util.List;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.product.dto.request.ProductSoldInfoWithoutSellerRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductSoldInfoWithSellerResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetProductSoldInfoWithSeller {

	private final MemberClient memberClient;

	public List<ProductSoldInfoWithSellerResponse> execute(List<ProductSoldInfoWithoutSellerRequest> requests,
		Long memberId) {
		return memberClient.getProductSoldInfoWithSeller(requests, memberId);
	}
}
