package org.oneog.uppick.domain.auction.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
	List<Auction> findAllByEndAtBeforeAndAuctionStatus(LocalDateTime now, AuctionStatus auctionStatus);
}
