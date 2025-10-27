package org.oneog.uppick.auction.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SoldProductInfoResponse {

	private final Long id;
	private final String name;
	private final String description;
	private final String image;
	private final Long finalPrice;
	private final LocalDateTime soldAt;

}
