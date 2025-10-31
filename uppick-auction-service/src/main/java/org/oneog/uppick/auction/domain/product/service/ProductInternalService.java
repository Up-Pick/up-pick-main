package org.oneog.uppick.auction.domain.product.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.oneog.uppick.auction.domain.auction.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.auction.service.AuctionInnerService;
import org.oneog.uppick.auction.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.service.CategoryInnerService;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.product.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.dto.request.SearchProductRequest;
import org.oneog.uppick.auction.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductDetailResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductSellAtResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.auction.domain.product.entity.Product;
import org.oneog.uppick.auction.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.auction.domain.product.mapper.ProductMapper;
import org.oneog.uppick.auction.domain.product.repository.ProductDocumentRepository;
import org.oneog.uppick.auction.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.auction.domain.product.repository.ProductRepository;
import org.oneog.uppick.auction.domain.searching.service.SearchingInnerService;
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
import org.springframework.web.multipart.MultipartFile;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductInternalService {

	// ****** search ****** //
	private final ElasticsearchOperations elasticsearchOperations;

	// ***** Product Domain ***** //
	private final ProductRepository productRepository;
	private final ProductDocumentRepository productDocumentRepository;
	private final ProductQueryRepository productQueryRepository;
	private final ProductMapper productMapper;
	private final ProductViewCountIncreaseService productViewCountIncreaseService;

	// ****** S3 ***** //
	private final S3FileManager s3FileManager;

	// ****** External Domain API ***** //
	private final AuctionInnerService auctionInnerService;
	private final MemberInnerService memberInnerService;
	private final SearchingInnerService searchingInnerService;
	private final CategoryInnerService categoryInnerService;

	private final AuctionRedisRepository auctionRedisRepository;

	// ***** Internal Service Method ***** //
	@Transactional
	public void registerProduct(ProductRegisterRequest request, MultipartFile image, Long registerId) {

		// 1. 이미지 검증
		if (image == null || image.isEmpty()) {
			throw new BusinessException(ProductErrorCode.EMPTY_FILE);
		}

		// 2. S3에 이미지 업로드
		String imageUrl = s3FileManager.store(image);

		// 3. Product 엔티티 생성 (imageUrl 포함)
		CategoryInfoResponse category = categoryInnerService.getCategoriesByCategoryId(request.getCategoryId());
		Product product = productMapper.registerToEntity(request, registerId, imageUrl, category);

		// 상품 및 경매 등록
		Product savedProduct = productRepository.save(product);
		auctionInnerService.registerAuction(savedProduct.getId(), registerId, request.getStartBid(),
			savedProduct.getRegisteredAt(), request.getEndAt());

		// Elasticsearch 저장
		ProductDocument document = productMapper.toDocument(savedProduct, request);
		productDocumentRepository.save(document);
	}

	@Transactional
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

	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {

		return productQueryRepository.getProductSimpleInfoById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_SIMPLE_INFO));
	}

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

	public Page<ProductBiddingInfoResponse> getBiddingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getBiddingProductInfoByMemberId(memberId, pageable);
	}

	public Page<ProductSellingInfoResponse> getSellingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getSellingProductInfoMyMemberId(memberId, pageable);
	}

	@Transactional
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

		// Main 모듈의 검색어 랭킹에 키워드 전달
		if (StringUtils.hasText(searchProductRequest.getKeyword())) {

			String[] splitKeywords = searchProductRequest.getKeyword().trim().split(" ");
			List<String> keywords = List.of(splitKeywords);
			searchingInnerService.saveSearchHistories(keywords);
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
