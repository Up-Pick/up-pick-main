package org.oneog.uppick.product.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterPurchaseDetailRequest {
	private Long auctionId;
	private Long buyerId;
	private Long productId;
	private Long price;
}
