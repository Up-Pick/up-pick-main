package org.oneog.uppick.auction.domain.product.command.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheEvictService {

	/**
	 * 상품 조회 캐시 삭제 (입찰 발생 시 호출)
	 * - productDetail: 상품 상세 조회 캐시
	 * - productSimpleInfo: 상품 간단 조회 캐시
	 */
	@CacheEvict(value = {"productDetail", "productSimpleInfo"}, key = "#productId")
	public void evictProductCache(Long productId) {

		log.info("🗑️ [캐시 삭제] 상품 조회 캐시 삭제 시도 - productId: {}, 캐시: [productDetail, productSimpleInfo]", productId);
	}

}
