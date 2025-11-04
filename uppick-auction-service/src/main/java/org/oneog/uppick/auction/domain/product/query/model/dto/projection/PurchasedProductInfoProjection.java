package org.oneog.uppick.auction.domain.product.query.model.dto.projection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PurchasedProductInfoProjection {

	private Long id;
	private String name;
	private String image;
	private Long finalPrice;

}
