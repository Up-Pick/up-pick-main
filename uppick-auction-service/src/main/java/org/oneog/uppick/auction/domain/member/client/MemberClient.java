package org.oneog.uppick.auction.domain.member.client;

import java.util.List;

import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.UpdateMemberCreditRequest;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellAtResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member-client", url = "${internal.main-service.url}")
public interface MemberClient {

	@GetMapping("/internal/v1/members/nickname")
	String getMemberNickname(@RequestParam Long memberId);

	@GetMapping("/internal/v1/sales/sell-at")
	List<ProductSellAtResponse> getSoldProductsSellAt(@RequestParam List<Long> productIds);

	@GetMapping("/internal/v1/purchases/buy-at")
	List<ProductBuyAtResponse> getPurchasedProductsBuyAt(@RequestParam List<Long> productIds);

	@GetMapping("/internal/v1/members/{memberId}/credit")
	long getMemberCredit(@PathVariable long memberId);

	@PostMapping("/internal/v1/members/{memberId}/credit")
	void updateMemberCredit(@PathVariable long memberId,
		@RequestBody UpdateMemberCreditRequest updateMemberCreditRequest);

	@PostMapping("/internal/v1/members/purchase-detail")
	void registerPurchaseDetail(@RequestParam RegisterPurchaseDetailRequest request);

	@PostMapping("/internal/v1/members/sell-detail")
	void registerSellDetail(@RequestParam RegisterSellDetailRequest request);

}