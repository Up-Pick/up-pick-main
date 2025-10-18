package org.oneog.uppick.domain.product.service;

public interface ProductExternalServiceApi {

	/**
	 * 경매 마감시 상품의 판매완료 날짜를 갱신?한다.
	 *
	 * @param productId 상품 ID
	 */
	default void changeProductsoldAt(Long productId) {

	}

	/**
	 *
	 * 경매가 유찰되어 더 이상 판매되지 않을 경우, 상품을 완전히 삭제
	 *
	 * @param productId 삭제할 상품의 ID
	 */
	default void deleteProduct(Long productId) {

	}
}
