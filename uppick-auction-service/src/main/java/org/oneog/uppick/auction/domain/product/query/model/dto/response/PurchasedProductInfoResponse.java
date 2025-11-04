package org.oneog.uppick.auction.domain.product.query.model.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PurchasedProductInfoResponse {

	private Long id;
	private String name;
	private String image;
	private Long finalPrice;
	private LocalDateTime buyAt;

}
