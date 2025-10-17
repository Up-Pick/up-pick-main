package org.oneog.uppick.domain.product.service;

public interface ProductExternalServiceApi {
	/**
	 * 상품 ID를 통해 판매자(memberId)를 조회하는 메서드.
	 *
	 * @param productId 상품 ID
	 * @return 판매자 회원 ID
	 */
	default Long findByProdcutId(Long productId) {
		return null;
	}
}
