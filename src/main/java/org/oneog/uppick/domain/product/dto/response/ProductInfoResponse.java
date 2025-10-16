package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductInfoResponse {

	private Long id;
	private String name;
	private String description;
	private Long viewCount;
	private LocalDate registeredAt;
	private String image;

	private String categoryName;
	private LocalDate soldAt;
	private Long currentBid;
	private String sellerName;
}