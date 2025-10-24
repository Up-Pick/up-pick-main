package org.oneog.uppick.domain.member.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSoldInfoWithSellerResponse {
	private final Long id;
	private final String name;
	private final String description;
	private final String image;
	private Long finalPrice;
	private LocalDateTime soldAt;
}
