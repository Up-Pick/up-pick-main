package org.oneog.uppick.domain.auction.repository;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
