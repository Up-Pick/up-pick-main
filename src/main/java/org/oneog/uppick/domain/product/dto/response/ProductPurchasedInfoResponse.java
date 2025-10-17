package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPurchasedInfoResponse {
	private Long id;
	private String name;
	private String image;
	private Long finalPrice;
	private LocalDateTime buyAt;
}
