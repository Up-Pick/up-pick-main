package org.oneog.uppick.domain.member.repository;

import org.oneog.uppick.domain.member.entity.SellDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellDetailRepository extends JpaRepository<SellDetail, Long> {
	SellDetail findByAuctionIdAndSellerId(Long auctionId, Long sellerId);
}
