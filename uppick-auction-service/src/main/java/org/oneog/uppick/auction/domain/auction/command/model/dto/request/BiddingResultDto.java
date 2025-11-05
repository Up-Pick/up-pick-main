package org.oneog.uppick.auction.domain.auction.command.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BiddingResultDto {

	private long productId;
	private long sellerId;
	private long biddingPrice;

}
