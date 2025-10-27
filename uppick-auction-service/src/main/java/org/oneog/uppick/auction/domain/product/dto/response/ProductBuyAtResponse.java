package org.oneog.uppick.auction.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductBuyAtResponse {

	private final Long id;
	private final LocalDateTime buyAt;

}
