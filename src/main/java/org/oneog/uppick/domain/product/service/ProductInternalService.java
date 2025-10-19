package org.oneog.uppick.domain.product.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.service.AuctionExternalServiceApi;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.entity.ProductViewHistory;
import org.oneog.uppick.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.domain.product.mapper.ProductMapper;
import org.oneog.uppick.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.oneog.uppick.domain.product.repository.ProductViewHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	// ****** External Domain API ***** //
	private final AuctionExternalServiceApi auctionExternalServiceApi;

	// ***** Internal Service Method ***** //
	@Transactional
	public void registerProduct(ProductRegisterRequest request, Long registerId) {

		Product product = productMapper.registerToEntity(request, registerId);

		// 상품 및 경매 등록
		productRepository.save(product);
		auctionExternalServiceApi.registerAuction(product.getId(), request.getStartBid(), product.getRegisteredAt(),
			request.getEndAt());
	}

	@Transactional
	public ProductInfoResponse getProductInfoById(Long productId, Long memberId) {

		// 조회수 +1
		Product product = findProductByIdOrElseThrow(productId);
		product.increaseViewCount();

		// 조회 내역 저장
		ProductViewHistory productViewHistory = new ProductViewHistory(productId, memberId);
		productViewHistoryRepository.save(productViewHistory);

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

	// ***** Internal Private Method ***** //
	private Product findProductByIdOrElseThrow(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}
}
