package org.oneog.uppick.auction.domain.product.dto.projection;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SearchProductProjection {

	private long id;
	private String image;
	private String name;
	private LocalDateTime registeredAt;
	private LocalDateTime endAt;
	private Long currentBidPrice;
	private long minBidPrice;
	private boolean isSold;

}