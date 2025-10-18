package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfoResponse {

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
	private final String sellerName;
}