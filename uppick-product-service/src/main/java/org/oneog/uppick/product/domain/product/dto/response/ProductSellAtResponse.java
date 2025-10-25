package org.oneog.uppick.product.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSellAtResponse {
	private final Long id;
	private final LocalDateTime buyAt;
}
