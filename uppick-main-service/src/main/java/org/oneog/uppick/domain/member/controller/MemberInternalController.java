package org.oneog.uppick.domain.member.controller;

import java.util.List;

import org.oneog.uppick.domain.member.dto.request.ProductPurchaseInfoWithoutBuyerRequest;
import org.oneog.uppick.domain.member.dto.request.ProductSoldInfoWithoutSellerRequest;
import org.oneog.uppick.domain.member.dto.response.ProductPurchaseInfoWithBuyerResponse;
import org.oneog.uppick.domain.member.dto.response.ProductSoldInfoWithSellerResponse;
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
@RequestMapping("/internal/v1/member")
public class MemberInternalController {

	private final MemberInternalService memberInternalService;

	@GetMapping("/nickname")
	public String getUserNickname(@RequestParam Long memberId) {
		return memberInternalService.getUserNicknameByMemberId(memberId);
	}

	@PostMapping("/{memberId}/set-seller-info")
	List<ProductSoldInfoWithSellerResponse> getProductSoldInfoWithSeller(
		@RequestBody List<ProductSoldInfoWithoutSellerRequest> requests,
		@PathVariable Long memberId) {
		return memberInternalService.setSellerToProductSoldInfo(requests, memberId);
	}

	@PostMapping("/{memberId}/set-buyer")
	List<ProductPurchaseInfoWithBuyerResponse> getProductPurchaseInfoWithBuyer(
		@RequestBody List<ProductPurchaseInfoWithoutBuyerRequest> requests,
		@PathVariable Long memberId) {
		return memberInternalService.setBuyerToProductPurchaseInfo(requests, memberId);
	}
}