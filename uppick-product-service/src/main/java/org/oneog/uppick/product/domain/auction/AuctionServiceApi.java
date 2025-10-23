package org.oneog.uppick.product.domain.auction;

import java.time.LocalDateTime;

public interface AuctionServiceApi {

    void registerAuction(Long id, Long startBid, LocalDateTime registeredAt, LocalDateTime endAt);

}
