package org.oneog.uppick.auction.domain.auction.common.mapper;

import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
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
