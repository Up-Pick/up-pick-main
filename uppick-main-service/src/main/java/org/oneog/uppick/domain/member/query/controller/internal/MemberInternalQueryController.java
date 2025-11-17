package org.oneog.uppick.domain.member.query.controller.internal;

import java.util.List;

import org.oneog.uppick.domain.member.query.model.dto.response.PurchasedProductBuyAtResponse;
import org.oneog.uppick.domain.member.query.model.dto.response.SoldProductSellAtResponse;
import org.oneog.uppick.common.dto.GlobalPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.oneog.uppick.domain.member.query.service.MemberInternalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class MemberInternalQueryController {

	private final MemberInternalQueryService memberInternalQueryService;

	@GetMapping("/members/nickname")
	public String getMemberNickname(@RequestParam Long memberId) {

		return memberInternalQueryService.getMemberNicknameByMemberId(memberId);
	}

	@GetMapping("/sales/sell-at")
	List<SoldProductSellAtResponse> getSoldProductsSellAt(@RequestParam List<Long> productIds) {

		return memberInternalQueryService.findSellAtByProductIds(productIds);
	}

	@GetMapping("/purchases/buy-at")
	List<PurchasedProductBuyAtResponse> getPurchasedProductsBuyAt(@RequestParam List<Long> productIds) {

		return memberInternalQueryService.findBuyAtByProductIds(productIds);
	}

	@GetMapping("/members/{memberId}/purchases/buy-at")
	public GlobalPageResponse<PurchasedProductBuyAtResponse> getPurchasedProductsBuyAtByMember(
		@PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {

		Page<PurchasedProductBuyAtResponse> page = memberInternalQueryService.findBuyAtByMemberId(memberId,
			pageable);

		return GlobalPageResponse.of(page);
	}

	@GetMapping("/members/{memberId}/sales/sell-at")
	public GlobalPageResponse<SoldProductSellAtResponse> getSoldProductsSellAtByMember(
		@PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {

		Page<SoldProductSellAtResponse> page = memberInternalQueryService.findSellAtByMemberId(memberId, pageable);

		return GlobalPageResponse.of(page);
	}

	@GetMapping("/members/{memberId}/credit")
	public long getMemberCredit(@PathVariable long memberId) {

		return memberInternalQueryService.getMemberCreditByMemberId(memberId);
	}

}
