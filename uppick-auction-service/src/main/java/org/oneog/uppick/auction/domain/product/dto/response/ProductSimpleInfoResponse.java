package org.oneog.uppick.auction.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSimpleInfoResponse {

	private final String name;
	private final String image;
	private final Long minBidPrice;
	private final Long currentBidPrice;

}
