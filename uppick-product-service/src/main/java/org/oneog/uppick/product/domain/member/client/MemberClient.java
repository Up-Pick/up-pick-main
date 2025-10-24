package org.oneog.uppick.product.domain.member.client;

import java.util.List;

import org.oneog.uppick.product.domain.product.dto.request.ProductPurchaseInfoWithoutBuyerRequest;
import org.oneog.uppick.product.domain.product.dto.request.ProductSoldInfoWithoutSellerRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductPurchaseInfoWithBuyerResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSoldInfoWithSellerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member-client", url = "${gateway.url}")
public interface MemberClient {

	@GetMapping("/main/internal/v1/member/nickname")
	String getUserNickname(@RequestParam Long memberId);

	@PostMapping("/main/internal/v1/member/{memberId}/set-seller")
	List<ProductSoldInfoWithSellerResponse> getProductSoldInfoWithSeller(
		@RequestBody List<ProductSoldInfoWithoutSellerRequest> requests,
		@PathVariable Long memberId);

	@PostMapping("/main/internal/v1/member/{memberId}/set-buyer")
	List<ProductPurchaseInfoWithBuyerResponse> getProductPurchaseInfoWithBuyer(
		@RequestBody List<ProductPurchaseInfoWithoutBuyerRequest> requests,
		@PathVariable Long memberId);
}