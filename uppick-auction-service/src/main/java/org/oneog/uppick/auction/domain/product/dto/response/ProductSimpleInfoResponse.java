package org.oneog.uppick.auction.domain.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSimpleInfoResponse {

	private String name;
	private String image;
	private Long minBidPrice;
	private Long currentBidPrice;

}
