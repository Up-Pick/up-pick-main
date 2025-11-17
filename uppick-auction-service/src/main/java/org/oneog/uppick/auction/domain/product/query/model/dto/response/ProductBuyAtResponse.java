package org.oneog.uppick.auction.domain.product.query.model.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductBuyAtResponse {

	private Long productId;
	private LocalDateTime buyAt;

}
