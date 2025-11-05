package org.oneog.uppick.auction.domain.auction.command.service;

import java.time.LocalDateTime;

public interface AuctionInnerCommandService {

	void registerAuction(Long id, Long registerId, Long startBid, LocalDateTime registeredAt, LocalDateTime endAt);

}
