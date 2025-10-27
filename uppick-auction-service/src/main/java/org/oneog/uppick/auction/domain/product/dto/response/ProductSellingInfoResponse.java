package org.oneog.uppick.auction.domain.product.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSellingInfoResponse {

	private final Long id;
	private final String name;
	private final String image;
	private final LocalDateTime endAt;
	private final Long currentBid;
	private final Long auctionId;

}
