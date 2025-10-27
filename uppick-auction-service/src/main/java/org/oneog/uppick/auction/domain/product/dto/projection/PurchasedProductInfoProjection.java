package org.oneog.uppick.auction.domain.product.dto.projection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchasedProductInfoProjection {

	private Long id;
	private String name;
	private String image;
	private Long finalPrice;

}
