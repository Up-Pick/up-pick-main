package org.oneog.uppick.domain.auction.repository;

import java.util.List;

import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiddingDetailRepository extends JpaRepository<BiddingDetail, Long> {
	List<Long> findDistinctMemberIdsByAuctionId(Long id);
}
