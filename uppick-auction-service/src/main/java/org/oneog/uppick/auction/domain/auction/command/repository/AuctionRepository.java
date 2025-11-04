package org.oneog.uppick.auction.domain.auction.command.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

	List<Auction> findAllByEndAtBeforeAndStatus(LocalDateTime now, AuctionStatus Status);

}
