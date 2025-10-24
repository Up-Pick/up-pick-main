package org.oneog.uppick.product.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ProductInfoResponse {

	private final Long id;
	private final String name;
	private final String description;
	private final Long viewCount;
	private final LocalDateTime registeredAt;
	private final String image;

	private final String categoryName;
	private final Long minPrice;
	private final Long currentBid;
	private final LocalDateTime endAt;
	private final Long sellerId;

	@Setter
	private String sellerName;

	public ProductInfoResponse(Long id, String name, String description, Long viewCount, LocalDateTime registeredAt,
		String image, String categoryName, Long minPrice, Long currentBid, LocalDateTime endAt, Long sellerId) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.viewCount = viewCount;
		this.registeredAt = registeredAt;
		this.image = image;
		this.categoryName = categoryName;
		this.minPrice = minPrice;
		this.currentBid = currentBid;
		this.endAt = endAt;
		this.sellerId = sellerId;
	}
}