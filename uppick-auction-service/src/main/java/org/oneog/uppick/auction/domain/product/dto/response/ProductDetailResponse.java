package org.oneog.uppick.auction.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDetailResponse {

	private final Long id;
	private final String name;
	private final String description;
	private final Long viewCount;
	private final LocalDateTime registeredAt;
	private final String image;

	private final String categoryName;
	private final Long minPrice;
	private final Long currentBid;
	private final LocalDateTime endAt;
	private String sellerName;

}