package org.oneog.uppick.product.domain.product.service;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.product.domain.auction.service.AuctionExternalService;
import org.oneog.uppick.product.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.product.domain.category.service.CategoryExternalServiceApi;
import org.oneog.uppick.product.domain.member.service.GetUserNicknameUseCase;
import org.oneog.uppick.product.domain.member.service.SetProductPurchaseInfoWithBuyer;
import org.oneog.uppick.product.domain.member.service.SetProductSoldInfoWithSeller;
import org.oneog.uppick.product.domain.product.dto.projection.SearchProductProjection;
import org.oneog.uppick.product.domain.product.dto.request.ProductPurchaseInfoWithoutBuyerRequest;
import org.oneog.uppick.product.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.product.domain.product.dto.request.ProductSoldInfoWithoutSellerRequest;
import org.oneog.uppick.product.domain.product.dto.request.SearchProductRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductPurchaseInfoWithBuyerResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSoldInfoWithSellerResponse;
import org.oneog.uppick.product.domain.product.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.product.domain.product.entity.Product;
import org.oneog.uppick.product.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.product.domain.product.mapper.ProductMapper;
import org.oneog.uppick.product.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.product.domain.product.repository.ProductRepository;
import org.oneog.uppick.product.domain.product.repository.SearchingQueryRepository;
import org.oneog.uppick.product.domain.searching.service.SaveSearchHistoriesUseCase;
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
	private final SaveSearchHistoriesUseCase saveSearchHistoriesUseCase;
	private final GetUserNicknameUseCase getUserNicknameUseCase;
	private final SetProductSoldInfoWithSeller setProductSoldInfoWithSeller;
	private final SetProductPurchaseInfoWithBuyer setProductPurchaseInfoWithBuyer;
	// ***** Product Domain ***** //
	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final SearchingQueryRepository searchingQueryRepository;
	private final ProductMapper productMapper;
	// ****** S3 ***** //
	private final S3FileManager s3FileManager;
	// ****** External Domain API ***** //
	private final AuctionExternalService auctionExternalServiceApi;
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
		auctionExternalServiceApi.registerAuction(product.getId(), registerId, request.getStartBid(),
			product.getRegisteredAt(), request.getEndAt());
	}

	@Transactional
	public ProductInfoResponse getProductInfoById(Long productId, AuthMember authMember) {

		if (authMember != null) {
			// 조회수 +1
			Product product = findProductByIdOrElseThrow(productId);
			product.increaseViewCount();
		}

		ProductInfoResponse response = productQueryRepository.getProductInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));
		response.setSellerName(getUserNicknameUseCase.execute(response.getSellerId()));

		return response;
	}

	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {
		return productQueryRepository.getProductSimpleInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_SIMPLE_INFO));
	}

	public Page<ProductSoldInfoWithSellerResponse> getProductSoldInfoByMemberId(Long memberId, Pageable pageable) {

		Page<ProductSoldInfoWithoutSellerRequest> requests = productQueryRepository.getProductSoldInfoByMemberId(
			memberId, pageable);
		List<ProductSoldInfoWithSellerResponse> responses = setProductSoldInfoWithSeller.execute(requests.getContent(),
			memberId);

		return new PageImpl<>(responses, requests.getPageable(), requests.getTotalElements());
	}

	public Page<ProductPurchaseInfoWithBuyerResponse> getPurchasedProductInfoByMemberId(Long memberId,
		Pageable pageable) {

		Page<ProductPurchaseInfoWithoutBuyerRequest> requests = productQueryRepository.getPurchasedProductInfoByMemberId(
			memberId, pageable);
		List<ProductPurchaseInfoWithBuyerResponse> responses = setProductPurchaseInfoWithBuyer.execute(
			requests.getContent(), memberId);

		return new PageImpl<>(responses, requests.getPageable(), requests.getTotalElements());
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
			saveSearchHistoriesUseCase.execute(keywords);
		}

		return productMapper.toResponse(productProjections);
	}
}
