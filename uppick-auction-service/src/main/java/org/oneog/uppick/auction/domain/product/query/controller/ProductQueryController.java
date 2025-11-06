package org.oneog.uppick.auction.domain.product.query.controller;

import org.oneog.uppick.auction.domain.product.query.model.dto.request.SearchProductRequest;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductDetailResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.service.ProductQueryService;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.common.dto.GlobalPageResponse;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductQueryController {

	private final ProductQueryService productQueryService;

	// 상품 상세 조회
	@GetMapping("/{productId}")
	public GlobalApiResponse<ProductDetailResponse> getProductInfo(@PathVariable Long productId,
		@AuthenticationPrincipal AuthMember authMember) {

		ProductDetailResponse response = productQueryService.getProductInfoById(productId, authMember);
		return GlobalApiResponse.ok(response);
	}

	// 입찰 시 상품 간단 조회
	@GetMapping("/{productId}/simple-info")
	public GlobalApiResponse<ProductSimpleInfoResponse> getProductSimpleInfo(@PathVariable Long productId) {

		ProductSimpleInfoResponse response = productQueryService.getProductSimpleInfoById(productId);
		return GlobalApiResponse.ok(response);
	}

	// 판매 완료된 상품 내역 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/sold/me")
	public GlobalPageResponse<SoldProductInfoResponse> getSoldProducts(@AuthenticationPrincipal AuthMember authMember,
		@PageableDefault(size = 20) Pageable pageable) {

		Page<SoldProductInfoResponse> responses = productQueryService.getSoldProductInfosByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 구매 완료 상품 내역 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/purchased/me")
	public GlobalPageResponse<PurchasedProductInfoResponse> getPurchasedProducts(
		@AuthenticationPrincipal AuthMember authMember, @PageableDefault(size = 20) Pageable pageable) {

		Page<PurchasedProductInfoResponse> responses = productQueryService.getPurchasedProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 입찰 중인 상품 목록 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/bidding/me")
	public GlobalPageResponse<ProductBiddingInfoResponse> getBiddingProducts(
		@AuthenticationPrincipal AuthMember authMember, @PageableDefault Pageable pageable) {

		Page<ProductBiddingInfoResponse> responses = productQueryService.getBiddingProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 경매 중인 상품 목록 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/selling/me")
	public GlobalPageResponse<ProductSellingInfoResponse> getSellingProducts(
		@AuthenticationPrincipal AuthMember authMember, @PageableDefault Pageable pageable) {

		Page<ProductSellingInfoResponse> responses = productQueryService.getSellingProductInfoByMemberId(
			authMember.getMemberId(), pageable);
		return GlobalPageResponse.of(responses);
	}

	// 상품 검색
	@PostMapping("/search")
	public GlobalApiResponse<GlobalPageResponse<SearchProductInfoResponse>> searchProduct(
		@RequestBody(required = false) SearchProductRequest searchProductRequest) {

		if (searchProductRequest == null) {
			searchProductRequest = SearchProductRequest.ofDefault();
		}

		return GlobalApiResponse.ok(
			GlobalPageResponse.of(productQueryService.searchProduct(searchProductRequest)));
	}

}
