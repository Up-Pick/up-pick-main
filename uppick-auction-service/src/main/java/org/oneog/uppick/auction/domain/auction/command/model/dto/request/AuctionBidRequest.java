package org.oneog.uppick.auction.domain.auction.command.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidRequest {

	@NotNull(message = "입찰가를 비울 수 없습니다.")
	@Positive(message = "양수만 입력이 가능합니다.")
	private Long biddingPrice;

}
