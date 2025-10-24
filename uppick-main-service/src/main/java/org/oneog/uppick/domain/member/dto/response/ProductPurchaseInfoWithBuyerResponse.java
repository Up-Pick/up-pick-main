package org.oneog.uppick.domain.member.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPurchaseInfoWithBuyerResponse {
	private final Long id;
	private final String name;
	private final String image;
	private final Long finalPrice;
	private final LocalDateTime buyAt;
}
