package org.oneog.uppick.product.domain.product.dto.projection;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchProductProjection {
	private final long id;
	private final String image;
	private final String name;
	private final LocalDateTime registeredAt;
	private final LocalDateTime endAt;
	private final Long currentBidPrice;
	private final long minBidPrice;
	private final boolean isSold;
}