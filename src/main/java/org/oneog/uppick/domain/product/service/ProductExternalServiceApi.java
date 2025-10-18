package org.oneog.uppick.domain.product.service;

public interface ProductExternalServiceApi {

	/**
	 * 경매 마감시 상품의 판매완료 날짜를 갱신?한다.
	 *
	 * @param productId 상품 ID
	 */
	void changeProductSoldAt(Long productId);
	
}
