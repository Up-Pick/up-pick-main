package org.oneog.uppick.auction.domain.product.query.service;

import org.oneog.uppick.auction.domain.product.common.exception.ProductErrorCode;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.query.model.dto.projection.ProductSimpleInfoProjection;
import org.oneog.uppick.auction.domain.product.query.repository.ProductQueryRepository;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

	private final ProductQueryRepository productQueryRepository;

	@Cacheable(value = "productDetail", key = "#productId")
	public ProductDetailProjection getProductDetailProjectionCached(Long productId) {

		log.info("[캐시 미스] productDetail 캐시 미스 발생 - DB 조회 시작: productId={}", productId);
		ProductDetailProjection projection = productQueryRepository.getProductInfoById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));
		log.info("[캐시 저장] productDetail 캐시에 저장 완료: productId={}", productId);
		return projection;
	}

	@Cacheable(value = "productSimpleInfo", key = "#productId")
	public ProductSimpleInfoProjection getProductSimpleInfoProjectionCached(Long productId) {

		log.info("[캐시 미스] productSimpleInfo 캐시 미스 발생 - DB 조회 시작: productId={}", productId);
		ProductSimpleInfoProjection projection = productQueryRepository.getProductSimpleInfoById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.CANNOT_READ_PRODUCT_INFO));
		log.info("[캐시 저장] productSimpleInfo 캐시에 저장 완료: productId={}", productId);
		return projection;
	}
}
