package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfoResponse {

	private Long id;
	private String name;
	private String description;
	private Long viewCount;
	private LocalDateTime registeredAt;
	private String image;

	private String categoryName;
	private LocalDateTime soldAt;
	private Long minPrice;
	private Long currentBid;
	private LocalDateTime endAt;
	private String sellerName;
}