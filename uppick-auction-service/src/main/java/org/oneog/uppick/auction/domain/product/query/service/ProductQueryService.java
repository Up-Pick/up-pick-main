package org.oneog.uppick.auction.domain.product.query.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.product.command.service.component.ProductViewCountIncreaseProcessor;
import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.common.exception.ProductErrorCode;
import org.oneog.uppick.auction.domain.product.common.mapper.ProductMapper;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.ProductSimpleInfoProjection;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.auction.domain.product.query.model.dto.request.SearchProductRequest;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductDetailResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellAtResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.repository.ProductOSRepository;
import org.oneog.uppick.auction.domain.product.query.repository.ProductQueryRepository;
import org.oneog.uppick.auction.domain.searching.service.SearchingInnerService;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

	private final ProductQueryRepository productQueryRepository;
	private final ProductMapper productMapper;
	private final ProductViewCountIncreaseProcessor viewCountIncreaseProcessor;
	private final MemberInnerService memberInnerService;
	private final AuctionRedisRepository auctionRedisRepository;
	private final ProductOSRepository productOSRepository;
	private final SearchingInnerService searchingInnerService;
	private final ProductCacheService productCacheService;

	@Transactional(readOnly = true)
	public ProductDetailResponse getProductInfoById(Long productId, AuthMember authMember) {

		// 상품 기본 정보만 캐싱
		ProductDetailProjection projection = productCacheService.getProductDetailProjectionCached(productId);

		if (authMember != null) {
			viewCountIncreaseProcessor.process(productId);
		}

		// 실시간 정보는 매번 조회
		Long currentPrice = auctionRedisRepository.findCurrentBidPrice(projection.getAuctionId());
		String sellerName = memberInnerService.getMemberNickname(projection.getSellerId());

		return productMapper.combineProductDetailWithSellerAndCurrentPrice(
			projection, sellerName, currentPrice);
	}

	@Transactional(readOnly = true)
	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {

		// 상품 기본 정보만 캐싱
		ProductSimpleInfoProjection projection = productCacheService.getProductSimpleInfoProjectionCached(productId);

		// 만약 마감 시간을 넘긴 경우 예외 처리
		if (LocalDateTime.now().isAfter(projection.getEndAt())) {
			throw new BusinessException(ProductErrorCode.NOT_ON_SALE_PRODUCT);
		}

		// 실시간 입찰가는 매번 조회
		Long currentPrice = auctionRedisRepository.findCurrentBidPrice(projection.getAuctionId());

		return productMapper.combineProductSimpleInfoResponseWithCurrentPrice(projection, currentPrice);
	}

	@Transactional(readOnly = true)
	public Page<SoldProductInfoResponse> getSoldProductInfosByMemberId(Long memberId, Pageable pageable) {

		// Member service에서 판매기록(Pageable) 먼저 조회
		Page<ProductSellAtResponse> sellAtPage = memberInnerService.getProductSellAtByMemberId(memberId, pageable);

		// member service가 반환한 목록의 productId를 추출
		List<Long> productIds = sellAtPage.getContent().stream().map(ProductSellAtResponse::getProductId).toList();

		// 일괄로 Product정보 조회
		List<SoldProductInfoProjection> productInfos = productQueryRepository.getProductSoldInfoByIds(productIds);

		// ProductList를 Map<ProductId, 객체> 형태로 변환
		Map<Long, SoldProductInfoProjection> productInfoMap = productInfos.stream()
			.collect(Collectors.toMap(SoldProductInfoProjection::getId, Function.identity()));

		// Member에서 제공한 순서(판매 시간 Desc)를 유지하면서 Product 정보와 결합
		List<SoldProductInfoResponse> contents = sellAtPage.getContent()
			.stream()
			.map(sellAtInfo -> {
				SoldProductInfoProjection productInfo = productInfoMap.get(sellAtInfo.getProductId());
				return productMapper.combineSoldProductInfoWithSeller(productInfo, sellAtInfo);
			})
			.filter(java.util.Objects::nonNull)
			.toList();

		return new PageImpl<>(contents, sellAtPage.getPageable(), sellAtPage.getTotalElements());
	}

	@Transactional(readOnly = true)
	public Page<PurchasedProductInfoResponse> getPurchasedProductInfoByMemberId(Long memberId,
		Pageable pageable) {

		Page<PurchasedProductInfoProjection> productPageInfo = productQueryRepository.getPurchasedProductInfoByMemberId(
			memberId, pageable);

		List<Long> productIds = productPageInfo.getContent()
			.stream()
			.map(PurchasedProductInfoProjection::getId)
			.toList();

		List<ProductBuyAtResponse> buyAtInfos = memberInnerService.getProductBuyAt(productIds);

		Map<Long, PurchasedProductInfoProjection> productInfoMap = productPageInfo.getContent()
			.stream()
			.collect(Collectors.toMap(PurchasedProductInfoProjection::getId, Function.identity()));

		List<PurchasedProductInfoResponse> contents = buyAtInfos
			.stream()
			.map(buyAtInfo -> {
				PurchasedProductInfoProjection productInfo = productInfoMap.get(buyAtInfo.getId());
				return productMapper.combinePurchasedInfoWithBuyer(productInfo, buyAtInfo);
			})
			.toList();

		return new PageImpl<>(contents, productPageInfo.getPageable(), productPageInfo.getTotalElements());
	}

	@Transactional(readOnly = true)
	public Page<ProductBiddingInfoResponse> getBiddingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getBiddingProductInfoByMemberId(memberId, pageable);
	}

	@Transactional(readOnly = true)
	public Page<ProductSellingInfoResponse> getSellingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getSellingProductInfoMyMemberId(memberId, pageable);
	}

	@Transactional(readOnly = true)
	public Page<SearchProductInfoResponse> searchProduct(SearchProductRequest searchProductRequest) {

		// 검색 히스토리 저장 (별도 트랜잭션 - MASTER DB)
		if (StringUtils.hasText(searchProductRequest.getKeyword())) {
			saveSearchHistory(searchProductRequest.getKeyword());
		}

		// Repository에서 검색 수행
		SearchHits<ProductDocument> searchHits = productOSRepository.searchProducts(searchProductRequest);

		// Document -> SearchResponse 매핑
		List<SearchProductInfoResponse> responseList = searchHits.getSearchHits().stream().map(searchHit -> {
			ProductDocument productDocument = searchHit.getContent();
			return productMapper.toSearchResponse(productDocument);
		}).toList();

		return new PageImpl<>(responseList, PageRequest.of(searchProductRequest.getPage(), searchProductRequest
			.getSize()), searchHits.getTotalHits());
	}

	// 헬퍼 메서드
	private void saveSearchHistory(String keyword) {

		String[] splitKeywords = keyword.trim().split(" ");
		List<String> keywords = List.of(splitKeywords);
		searchingInnerService.saveSearchHistories(keywords);
	}

}
