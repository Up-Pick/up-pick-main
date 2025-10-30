package org.oneog.uppick.auction.domain.auction.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum AuctionEventType {

    BID_PLACED("bid.placed"), AUCTION_ENDED("auction.ended");

    private final String key;

    public String asRoutingKey() {

        return key;
    }

}
