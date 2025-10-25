package org.oneog.uppick.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterSellDetailRequest {
	private Long auctionId;
	private Long sellerId;
	private Long productId;
	private Long price;
}
