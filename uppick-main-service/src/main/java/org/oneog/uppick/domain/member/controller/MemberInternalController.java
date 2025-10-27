package org.oneog.uppick.domain.member.controller;

import java.util.List;

import org.oneog.uppick.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.domain.member.dto.request.UpdateMemberCreditRequest;
import org.oneog.uppick.domain.member.dto.response.PurchasedProductBuyAtResponse;
import org.oneog.uppick.domain.member.dto.response.SoldProductSellAtResponse;
import org.oneog.uppick.domain.member.service.MemberInternalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class MemberInternalController {

	private final MemberInternalService memberInternalService;

	@GetMapping("/members/nickname")
	public String getMemberNickname(@RequestParam Long memberId) {

		return memberInternalService.getMemberNicknameByMemberId(memberId);
	}

	@GetMapping("/sales/sell-at")
	List<SoldProductSellAtResponse> getSoldProductsSellAt(@RequestParam List<Long> productIds) {

		return memberInternalService.findSellAtByProductIds(productIds);
	}

	@GetMapping("/purchases/buy-at")
	List<PurchasedProductBuyAtResponse> getPurchasedProductsBuyAt(@RequestParam List<Long> productIds) {

		return memberInternalService.findBuyAtByProductIds(productIds);
	}

	@GetMapping("/members/{memberId}/credit")
	public long getMemberCredit(@PathVariable long memberId) {

		return memberInternalService.getMemberCreditByMemberId(memberId);
	}

	@PostMapping("/members/{memberId}/credit")
	public void updateMemberCredit(@PathVariable long memberId,
		@RequestBody UpdateMemberCreditRequest updateMemberCreditRequest) {

		memberInternalService.updateMemberCredit(memberId, updateMemberCreditRequest.getAmount());
	}

	@PostMapping("/members/purchase-detail")
	public void registerPurchaseDetail(@RequestParam RegisterPurchaseDetailRequest request) {

		memberInternalService.registerPurchaseDetail(request);
	}

	@PostMapping("/members/sell-detail")
	public void registerSellDetail(@RequestParam RegisterSellDetailRequest request) {

		memberInternalService.registerSellDetail(request);

	}

}