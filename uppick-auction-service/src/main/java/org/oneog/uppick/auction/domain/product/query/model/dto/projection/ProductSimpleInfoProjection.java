package org.oneog.uppick.auction.domain.product.query.model.dto.projection;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductSimpleInfoProjection {

	private String name;
	private String image;
	private LocalDateTime endAt;
	private Long minBidPrice;
	private Long auctionId;

}
