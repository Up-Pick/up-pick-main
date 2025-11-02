package org.oneog.uppick.batch.domain.bidprice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 입찰가 배치 DTO
 * Redis에서 읽은 입찰가 데이터를 Elasticsearch로 전달
 */
@Getter
@AllArgsConstructor
public class BidPriceDto {

	private Long auctionId;  // 경매 ID
	private Long productId;  // 상품 ID (Elasticsearch 업데이트용)
	private Long bidPrice;   // 현재 입찰가

}
