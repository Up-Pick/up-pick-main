package org.oneog.uppick.domain.member.command.model.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegisterSellDetailRequest {

	private Long auctionId;
	private Long sellerId;
	private Long productId;
	private Long price;

}