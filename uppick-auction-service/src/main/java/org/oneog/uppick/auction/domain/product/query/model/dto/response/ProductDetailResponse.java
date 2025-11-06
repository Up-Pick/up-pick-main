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
public class ProductDetailResponse {

	private Long id;
	private String name;
	private String description;
	private Long viewCount;
	private LocalDateTime registeredAt;
	private String image;

	private String categoryName;
	private Long minPrice;
	private Long currentBid;
	private LocalDateTime endAt;
	private String sellerName;

}