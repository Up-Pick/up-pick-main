package org.oneog.uppick.auction.domain.auction.event;

import org.oneog.uppick.common.event.DomainEvent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@Jacksonized
@SuperBuilder
public class BidPlacedEvent extends DomainEvent {

    private final long sellerId;
    private final long bidderId;
    private final long auctionId;
    private final long biddingPrice;

}
