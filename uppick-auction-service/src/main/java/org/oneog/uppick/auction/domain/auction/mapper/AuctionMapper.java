package org.oneog.uppick.auction.domain.auction.mapper;

import org.oneog.uppick.auction.domain.auction.entity.BiddingDetail;
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
