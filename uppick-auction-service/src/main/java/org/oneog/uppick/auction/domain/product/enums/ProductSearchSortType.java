package org.oneog.uppick.auction.domain.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductSearchSortType {
	CURRENT_BID_ASC("current_bid_price:asc"),
	CURRENT_BID_DESC("current_bid_price:desc"),
	REGISTERED_AT_DESC("registered_at:desc"),
	END_AT_DESC("end_at:desc");

	private final String sortType;
}
