package org.oneog.uppick.auction.domain.auction.command.repository;

import java.util.Optional;

import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiddingDetailRepository extends JpaRepository<BiddingDetail, Long> {

	Optional<BiddingDetail> findTopByAuctionIdAndBidderIdAndBidPrice(long auctionId, Long bidderId, Long bidPrice);

}
