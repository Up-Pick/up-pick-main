package org.oneog.uppick.domain.member.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegisterPurchaseDetailRequest {

	private Long auctionId;
	private Long buyerId;
	private Long productId;
	private Long price;

}