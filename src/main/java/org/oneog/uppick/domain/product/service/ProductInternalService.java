package org.oneog.uppick.domain.product.service;

import java.util.List;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.service.AuctionExternalServiceApi;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.domain.product.mapper.ProductMapper;
import org.oneog.uppick.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
	private final ProductMapper productMapper;

	// ****** External Domain API ***** //
	private final AuctionExternalServiceApi auctionExternalServiceApi;

	// ***** Service Method ***** //
	@Transactional
	public void registerProduct(ProductRegisterRequest request, Long registerId) {

		Product product = productMapper.registerToEntity(request, registerId);

		// 상품 및 경매 등록
		productRepository.save(product);
		auctionExternalServiceApi.registerAuction(product.getId(), request.getStartBid(), request.getEndAt());
	}

	@Transactional
	public ProductInfoResponse getProductInfoById(Long productId) {

		// 조회수 +1
		Product product = findProductByIdOrElseThrow(productId);
		product.increaseViewCount();

		return productQueryRepository.getProductInfoById(productId).orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));
	}

	public Page<ProductSoldInfoResponse> getProductSoldInfoByMemberId(Long memberId) {

		List<ProductSoldInfoResponse> responseList = productQueryRepository.getProductSoldInfoByMemberId(memberId);
		Pageable pageable = PageRequest.of(0, 10);
		long totalCount = responseList.size();

		return new PageImpl<>(responseList, pageable, totalCount);
	}

	// ***** Internal Method ***** //
	private Product findProductByIdOrElseThrow(Long productId) {
		return productRepository.findById(productId).orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}
}
