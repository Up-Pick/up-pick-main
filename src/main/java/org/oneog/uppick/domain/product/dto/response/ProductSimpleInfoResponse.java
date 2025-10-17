package org.oneog.uppick.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSimpleInfoResponse {
	private String name;
	private String image;
	private Long minBidPrice;
	private Long currentBidPrice;
}
