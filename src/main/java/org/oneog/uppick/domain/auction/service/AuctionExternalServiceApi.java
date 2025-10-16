package org.oneog.uppick.domain.auction.service;

import java.time.LocalDateTime;

public interface AuctionExternalServiceApi {

	/**
	 * Product 도메인의 '판매 상품 등록 API' 에서 상품을 등록하며, 동시에 Auction 도메인에서 해당 경매 데이터 등록
	 * @param productID 상품 Id
	 * @param minPrice 최소 입찰가
	 * @param endAt 마감 시간
	 */
	void registerAuction(Long productID, Long minPrice, LocalDateTime endAt);
}
