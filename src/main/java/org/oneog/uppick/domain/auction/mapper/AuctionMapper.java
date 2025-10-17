package org.oneog.uppick.domain.auction.mapper;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.Status;
import org.springframework.stereotype.Component;

@Component
public class AuctionMapper {

	public Auction registerToEntity(Long productID, Long minPrice, LocalDateTime endAt) {
		return Auction.builder()
			.productId(productID)
			.currentPrice(null)
			.minPrice(minPrice)
			.status(Status.IN_PROGRESS)
			.endAt(endAt)
			.build();
	}

	public BiddingDetail toEntity(Long auctionId, Long memberId, Long bidPrice) {
		return BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(bidPrice)
			.build();
	}
}
