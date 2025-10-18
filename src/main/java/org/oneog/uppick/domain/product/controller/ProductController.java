package org.oneog.uppick.domain.product.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.common.dto.GlobalPageResponse;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductInternalService productInternalService;

	// 판매 상품 등록
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public GlobalApiResponse<Void> registerProduct(
		@Valid @RequestBody ProductRegisterRequest request,
		@AuthenticationPrincipal AuthMember authMember) {

		productInternalService.registerProduct(request, authMember.getMemberId());
		return GlobalApiResponse.ok(null);
	}

	// 상품 상세 조회
	@GetMapping("/{productId}")
	public GlobalApiResponse<ProductInfoResponse> getProductInfo(@PathVariable Long productId) {

		ProductInfoResponse response = productInternalService.getProductInfoById(productId);
		return GlobalApiResponse.ok(response);
	}

	// 입찰 시 상품 간단 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{productId}/simple-info")
	public GlobalApiResponse<ProductSimpleInfoResponse> getProductSimpleInfo(@PathVariable Long productId) {

		ProductSimpleInfoResponse response = productInternalService.getProductSimpleInfoById(productId);
		return GlobalApiResponse.ok(response);
	}

	// 판매 완료된 상품 내역 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/sold/me")
	public GlobalPageResponse<ProductSoldInfoResponse> getSoldProducts(
		@AuthenticationPrincipal AuthMember authMember,
		@PageableDefault(size = 20) Pageable pageable) {

		Page<ProductSoldInfoResponse> responses = productInternalService.getProductSoldInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 구매 완료 상품 내역 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/purchased/me")
	public GlobalPageResponse<ProductPurchasedInfoResponse> getPurchasedProducts(
		@AuthenticationPrincipal AuthMember authMember,
		@PageableDefault(size = 20) Pageable pageable) {

		Page<ProductPurchasedInfoResponse> responses = productInternalService.getPurchasedProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 입찰 중인 상품 목록 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/bidding/me")
	public GlobalPageResponse<ProductBiddingInfoResponse> getBiddingProducts(
		@AuthenticationPrincipal AuthMember authMember,
		@PageableDefault Pageable pageable) {

		Page<ProductBiddingInfoResponse> responses = productInternalService.getBiddingProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 경매 중인 상품 목록 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/selling/me")
	public GlobalPageResponse<ProductSellingInfoResponse> getSellingProducts(
		@AuthenticationPrincipal AuthMember authMember,
		@PageableDefault Pageable pageable) {

		Page<ProductSellingInfoResponse> responses = productInternalService.getSellingProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}
}
