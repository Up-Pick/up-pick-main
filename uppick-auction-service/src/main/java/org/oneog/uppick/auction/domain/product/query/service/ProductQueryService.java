package org.oneog.uppick.auction.domain.product.query.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.product.command.service.ProductViewCountIncreaseService;
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
import org.oneog.uppick.auction.domain.product.query.repository.ProductQueryRepository;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

	private final ProductQueryRepository productQueryRepository;
	private final ProductMapper productMapper;
	private final ProductViewCountIncreaseService productViewCountIncreaseService;
	private final MemberInnerService memberInnerService;
	private final AuctionRedisRepository auctionRedisRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final SearchHistoryService searchHistoryService;

	@Transactional(readOnly = true)
	public ProductDetailResponse getProductInfoById(Long productId, AuthMember authMember) {

		ProductDetailProjection projection = productQueryRepository.getProductInfoById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));

		if (authMember != null) {
			productViewCountIncreaseService.increaseProductViewCount(productId);
		}

		String sellerName = memberInnerService.getMemberNickname(projection.getSellerId());
		Long currentPrice = auctionRedisRepository.findCurrentBidPrice(projection.getAuctionId());

		return productMapper.combineProductDetailWithSellerAndCurrentPrice(projection, sellerName, currentPrice);
	}

	@Transactional(readOnly = true)
	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {

		ProductSimpleInfoProjection projection = productQueryRepository.getProductSimpleInfoById(
				productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));

		Long currentPrice = auctionRedisRepository.findCurrentBidPrice(projection.getAuctionId());

		return productMapper.combineProductSimpleInfoResponseWithCurrentPrice(projection, currentPrice);
	}

	@Transactional(readOnly = true)
	public Page<SoldProductInfoResponse> getSoldProductInfosByMemberId(Long memberId, Pageable pageable) {

		// Product Page 조회
		Page<SoldProductInfoProjection> productPageInfo = productQueryRepository.getProductSoldInfoByMemberId(
			memberId, pageable);

		// Product Ids만 추출한 List로 추출한 뒤, Member에 전달
		List<Long> productIds = productPageInfo.getContent()
			.stream()
			.map(SoldProductInfoProjection::getId)
			.toList();

		// List<ProductId> -> List<ProductSellerInfoResponse> (각각의 id 값마다 seller Info 포함된 객체, 판매 시간 내림차순으로 반환받음)
		List<ProductSellAtResponse> sellAtInfos = memberInnerService.getProductSellAt(productIds);

		// ProductList를 Map<ProductId, 객체> 형태로 변환
		Map<Long, SoldProductInfoProjection> productInfoMap = productPageInfo.getContent()
			.stream()
			.collect(Collectors.toMap(SoldProductInfoProjection::getId, Function.identity()));

		// 정렬되었던 List<ProductId> 기준으로 ProductInfo와 결합한 뒤 반환
		List<SoldProductInfoResponse> contents = sellAtInfos
			.stream()
			.map(sellAtInfo -> {
				SoldProductInfoProjection productInfo = productInfoMap.get(sellAtInfo.getId());
				return productMapper.combineSoldProductInfoWithSeller(productInfo, sellAtInfo);
			})
			.toList();

		return new PageImpl<>(contents, productPageInfo.getPageable(), productPageInfo.getTotalElements());
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

		// 정렬 기준
		String[] sortType = searchProductRequest.getSortBy().getSortType().split(":");

		NativeQuery query = NativeQuery.builder()
			.withQuery(
				Query.of(q -> q
					.bool(b -> {

						// 상품 이름 : match 조회
						b.must(m -> m.match(mq -> mq
							.field("name")
							.query(searchProductRequest.getKeyword())
							.fuzziness("AUTO"))
						);

						// 기본 값 1L 또는 지정된 category_id 조회
						b.filter(f -> f.term(t -> t
							.field("category_id")
							.value(searchProductRequest.getCategoryId())
						));

						// onlyNotSold == false 경우: 판매 안 된 상품만 조회
						if (!searchProductRequest.isOnlyNotSold()) {
							b.filter(f -> f.term(t -> t
								.field("is_sold")
								.value(false)
							));
						}

						// 최소 마감 날짜 기준이 null이 아닌 경우 필터링
						if (searchProductRequest.getEndAtFrom() != null) {
							b.filter(f -> f.range(r -> r
								.date(d -> d
									.field("end_at")
									.gte(searchProductRequest.getEndAtFrom().toString())
								)
							));
						}

						return b;
					})
				)
			)
			.withPageable(PageRequest.of(
				searchProductRequest.getPage(),
				searchProductRequest.getSize()))
			// 정렬 기준
			.withSort(s -> s
				.field(f -> f
					.field(sortType[0])
					.order(sortType[1].equals("asc") ? SortOrder.Asc : SortOrder.Desc)
					.missing("_last")
				))
			.build();

		// 검색 히스토리 저장 (별도 트랜잭션 - MASTER DB)
		if (StringUtils.hasText(searchProductRequest.getKeyword())) {
			searchHistoryService.saveSearchHistory(searchProductRequest.getKeyword());
		}

		// Document -> SearchResponse 매핑
		SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
		List<SearchProductInfoResponse> responseList = searchHits.getSearchHits().stream().map(searchHit -> {
			ProductDocument productDocument = searchHit.getContent();
			return productMapper.toSearchResponse(productDocument);
		}).toList();

		return new PageImpl<>(responseList, query.getPageable(), searchHits.getTotalHits());
	}

}
