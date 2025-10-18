package org.oneog.uppick.domain.auction.mapper;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.springframework.stereotype.Component;

@Component
public class AuctionMapper {

	public Auction registerToEntity(Long productID, Long minPrice, LocalDateTime registeredAt, LocalDateTime endAt) {
		return Auction.builder()
			.productId(productID)
			.currentPrice(null)
			.minPrice(minPrice)
			.status(AuctionStatus.IN_PROGRESS)
			.registeredAt(registeredAt)
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
