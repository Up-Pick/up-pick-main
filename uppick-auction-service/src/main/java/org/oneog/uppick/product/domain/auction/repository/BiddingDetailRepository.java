package org.oneog.uppick.product.domain.auction.repository;

import java.util.Optional;

import org.oneog.uppick.product.domain.auction.entity.BiddingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiddingDetailRepository extends JpaRepository<BiddingDetail, Long> {

	Optional<BiddingDetail> findTopByAuctionIdOrderByBidPriceDesc(Long auctionId);

}
