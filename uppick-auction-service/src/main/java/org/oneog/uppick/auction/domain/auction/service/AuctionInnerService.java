package org.oneog.uppick.auction.domain.auction.service;

import java.time.LocalDateTime;

public interface AuctionInnerService {

	void registerAuction(Long id, Long registerId, Long startBid, LocalDateTime registeredAt, LocalDateTime endAt);

}
