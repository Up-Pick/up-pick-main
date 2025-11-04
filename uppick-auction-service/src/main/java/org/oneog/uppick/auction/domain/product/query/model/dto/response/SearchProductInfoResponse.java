package org.oneog.uppick.auction.domain.product.query.model.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SearchProductInfoResponse {

	private long id;
	private String image;
	private String name;
	private LocalDateTime registeredAt;
	private LocalDateTime endAt;
	private Long currentBidPrice;
	private long minBidPrice;
	private boolean isSold;

}
