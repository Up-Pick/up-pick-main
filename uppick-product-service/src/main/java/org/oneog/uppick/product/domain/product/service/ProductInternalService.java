package org.oneog.uppick.product.domain.product.service;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.product.domain.auction.AuctionServiceApi;
import org.oneog.uppick.product.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductRecentViewInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.product.domain.product.entity.Product;
import org.oneog.uppick.product.domain.product.entity.ProductViewHistory;
import org.oneog.uppick.product.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.product.domain.product.mapper.ProductMapper;
import org.oneog.uppick.product.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.product.domain.product.repository.ProductRepository;
import org.oneog.uppick.product.domain.product.repository.ProductViewHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductInternalService {

	// ***** Product Domain ***** //
	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final ProductViewHistoryRepository productViewHistoryRepository;

	private final ProductMapper productMapper;

	// ****** S3 ***** //
	private final S3FileManager s3FileManager;

	// ****** External Domain API ***** //
	private final AuctionServiceApi auctionExternalServiceApi;

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
		Product product = productMapper.registerToEntity(request, registerId, imageUrl);

		// 상품 및 경매 등록
		productRepository.save(product);
		auctionExternalServiceApi.registerAuction(product.getId(), request.getStartBid(), product.getRegisteredAt(),
			request.getEndAt());
	}

	@Transactional
	public ProductInfoResponse getProductInfoById(Long productId, AuthMember authMember) {

		if (authMember != null) {
			// 조회수 +1
			Product product = findProductByIdOrElseThrow(productId);
			product.increaseViewCount();

			// 조회 내역 저장
			ProductViewHistory productViewHistory = new ProductViewHistory(productId, authMember.getMemberId());
			productViewHistoryRepository.save(productViewHistory);
		}

		return productQueryRepository.getProductInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));
	}

	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {
		return productQueryRepository.getProductSimpleInfoById(productId).orElseThrow(
			() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_SIMPLE_INFO));
	}

	public Page<ProductSoldInfoResponse> getProductSoldInfoByMemberId(Long memberId, Pageable pageable) {
		return productQueryRepository.getProductSoldInfoByMemberId(memberId, pageable);
	}

	public Page<ProductPurchasedInfoResponse> getPurchasedProductInfoByMemberId(Long memberId, Pageable pageable) {
		return productQueryRepository.getPurchasedProductInfoByMemberId(memberId, pageable);
	}

	public Page<ProductBiddingInfoResponse> getBiddingProductInfoByMemberId(Long memberId, Pageable pageable) {
		return productQueryRepository.getBiddingProductInfoByMemberId(memberId, pageable);
	}

	public Page<ProductSellingInfoResponse> getSellingProductInfoByMemberId(Long memberId, Pageable pageable) {
		return productQueryRepository.getSellingProductInfoMyMemberId(memberId, pageable);
	}

	public Page<ProductRecentViewInfoResponse> getRecentViewProductInfoByMemberId(Long memberId, Pageable pageable) {
		return productQueryRepository.getRecentViewProductInfoByMemberId(memberId, pageable);
	}

	// ***** Internal Private Method ***** //
	private Product findProductByIdOrElseThrow(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}
}
