package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRecentViewInfoResponse {
	private Long id;
	private String name;
	private String image;
	private Long currentBid;
	private LocalDateTime endAt;
	private LocalDateTime viewedAt;
}
