package org.oneog.uppick.domain.auction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuctionBidRequest {

	@NotNull(message = "입찰가를 비울 수 없습니다.")
	private Long biddingPrice;
}
