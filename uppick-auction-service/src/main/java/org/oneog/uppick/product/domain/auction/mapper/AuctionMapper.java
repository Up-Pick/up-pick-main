package org.oneog.uppick.product.domain.auction.mapper;

import org.oneog.uppick.product.domain.auction.entity.BiddingDetail;
import org.springframework.stereotype.Component;

@Component
public class AuctionMapper {

	public BiddingDetail toEntity(Long auctionId, Long memberId, Long bidPrice) {

		return BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(bidPrice)
			.build();
	}

}
