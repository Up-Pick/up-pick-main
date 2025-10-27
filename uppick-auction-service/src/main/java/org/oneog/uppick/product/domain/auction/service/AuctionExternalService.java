package org.oneog.uppick.product.domain.auction.service;

import java.time.LocalDateTime;

public interface AuctionExternalService {

	void registerAuction(Long id, Long registerId, Long startBid, LocalDateTime registeredAt, LocalDateTime endAt);

}
