package org.oneog.uppick.product.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPurchaseInfoWithBuyerResponse {
	private final Long id;
	private final String name;
	private final String image;
	private final Long finalPrice;
	private final LocalDateTime buyAt;
}
