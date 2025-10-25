package org.oneog.uppick.product.domain.member.client;

import java.util.List;

import org.oneog.uppick.product.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellAtResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member-client", url = "${gateway.url}")
public interface MemberClient {

	@GetMapping("/main/internal/v1/members/nickname")
	String getMemberNickname(@RequestParam Long memberId);

	@GetMapping("/main/internal/v1/sales/sell-at")
	List<ProductSellAtResponse> getSoldProductsSellAt(@RequestParam List<Long> productIds);

	@GetMapping("/main/internal/v1/purchases/buy-at")
	List<ProductBuyAtResponse> getPurchasedProductsBuyAt(@RequestParam List<Long> productIds);
}