package org.oneog.uppick.auction.domain.auction.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum AuctionEventType {

    BID_PLACED("bid.placed");

    private final String key;

    public String asRoutingKey() {

        return key;
    }

}
