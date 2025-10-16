package org.oneog.uppick.domain.bid.repository;

import org.oneog.uppick.domain.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
