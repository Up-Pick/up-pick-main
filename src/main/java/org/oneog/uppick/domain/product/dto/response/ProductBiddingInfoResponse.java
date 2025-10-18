package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductBiddingInfoResponse {
	private Long id;
	private String name;
	private String image;
	private LocalDateTime endAt;
	private Long currentBid;
	private Long bidPrice;
}
