package org.oneog.uppick.product.domain.product.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.product.domain.auction.service.AuctionInnerService;
import org.oneog.uppick.product.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.product.domain.category.service.CategoryExternalServiceApi;
import org.oneog.uppick.product.domain.member.service.MemberInnerService;
import org.oneog.uppick.product.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.product.domain.product.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.product.domain.product.dto.projection.SearchProductProjection;
import org.oneog.uppick.product.domain.product.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.product.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.product.domain.product.dto.request.SearchProductRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductDetailResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.product.domain.product.entity.Product;
import org.oneog.uppick.product.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.product.domain.product.mapper.ProductMapper;
import org.oneog.uppick.product.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.product.domain.product.repository.ProductRepository;
import org.oneog.uppick.product.domain.product.repository.SearchingQueryRepository;
import org.oneog.uppick.product.domain.searching.service.SearchingInnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductInternalService {

	// ****** search ****** //
	private static final long DEFAULT_CATEGORY_ID = 1L;
	private static final boolean DEFAULT_ONLY_NOT_SOLD = false;
	private static final int DEFAULT_SIZE = 20;

	// ***** Product Domain ***** //
	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final SearchingQueryRepository searchingQueryRepository;
	private final ProductMapper productMapper;

	// ****** S3 ***** //
	private final S3FileManager s3FileManager;

	// ****** External Domain API ***** //
	private final AuctionInnerService auctionInnerService;
	private final MemberInnerService memberInnerService;
	private final SearchingInnerService searchingInnerService;
	private final CategoryExternalServiceApi categoryExternalServiceApi;

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
		CategoryInfoResponse category = categoryExternalServiceApi.getCategoriesByCategoryId(request.getCategoryId());
		Product product = productMapper.registerToEntity(request, registerId, imageUrl, category);

		// 상품 및 경매 등록
		productRepository.save(product);
		auctionInnerService.registerAuction(product.getId(), registerId, request.getStartBid(),
			product.getRegisteredAt(), request.getEndAt());
	}

	@Transactional
	public ProductDetailResponse getProductInfoById(Long productId, AuthMember authMember) {

		if (authMember != null) {
			// 조회수 +1
			Product product = findProductByIdOrElseThrow(productId);
			product.increaseViewCount();
		}

		ProductDetailProjection projection = productQueryRepository.getProductInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));

		String sellerName = memberInnerService.getMemberNickname(projection.getSellerId());

		return productMapper.combineProductDetailWithSeller(projection, sellerName);
	}

	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {

		return productQueryRepository.getProductSimpleInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_SIMPLE_INFO));
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
			}).toList();

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
			}).toList();

		return new PageImpl<>(contents, productPageInfo.getPageable(), productPageInfo.getTotalElements());
	}

	public Page<ProductBiddingInfoResponse> getBiddingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getBiddingProductInfoByMemberId(memberId, pageable);
	}

	public Page<ProductSellingInfoResponse> getSellingProductInfoByMemberId(Long memberId, Pageable pageable) {

		return productQueryRepository.getSellingProductInfoMyMemberId(memberId, pageable);
	}

	// ***** Internal Private Method ***** //
	private Product findProductByIdOrElseThrow(Long productId) {

		return productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}

	@Transactional
	public Page<SearchProductInfoResponse> searchProduct(SearchProductRequest searchProductRequest) {

		Long categoryId = searchProductRequest.getCategoryId();

		if (categoryId == null) {
			categoryId = DEFAULT_CATEGORY_ID;
		}

		Boolean onlyNotSold = searchProductRequest.getOnlyNotSold();

		if (onlyNotSold == null) {
			onlyNotSold = DEFAULT_ONLY_NOT_SOLD;
		}

		Integer page = searchProductRequest.getPage();

		if (page == null || page < 0) {
			page = 0;
		}

		Integer size = searchProductRequest.getSize();

		if (size == null || size <= 0) {
			size = DEFAULT_SIZE;
		}

		Pageable pageable = PageRequest.of(page, size);

		LocalDateTime endAtFrom;

		if (searchProductRequest.getEndAtFrom() != null) {
			endAtFrom = searchProductRequest.getEndAtFrom().atStartOfDay();
		} else {
			endAtFrom = null;
		}

		Page<SearchProductProjection> productProjections = searchingQueryRepository.findProductsWithFilters(
			pageable,
			categoryId,
			endAtFrom,
			onlyNotSold,
			searchProductRequest.getSortBy(),
			searchProductRequest.getKeyword());

		if (StringUtils.hasText(searchProductRequest.getKeyword())) {

			String[] splitKeywords = searchProductRequest.getKeyword().trim().split(" ");
			List<String> keywords = List.of(splitKeywords);
			searchingInnerService.saveSearchHistories(keywords);
		}

		return productMapper.toResponse(productProjections);
	}

}
