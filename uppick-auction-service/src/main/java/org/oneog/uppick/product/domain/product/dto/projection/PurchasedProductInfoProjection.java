package org.oneog.uppick.product.domain.product.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchasedProductInfoProjection {

	private final Long id;
	private final String name;
	private final String image;
	private final Long finalPrice;

}
