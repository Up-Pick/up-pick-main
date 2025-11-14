package org.oneog.uppick.auction.domain.auction.common.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class AuctionRedisConstant {

	public static final String CURRENT_BID_PRICE_KEY = "auction:%d:current-bid-price";
	public static final String LAST_BIDDER_ID_KEY = "auction:%d:last-bidder-id";
	public static final String OPENSEARCH_SYNC_FLAG_KEY = "opensearch:sync:%d";

	public final DefaultRedisScript<Void> biddingScript;

	public AuctionRedisConstant() {

		biddingScript = new DefaultRedisScript<>();

		biddingScript.setLocation(
			new ClassPathResource("redis/bidding.lua"));
		biddingScript.setResultType(Void.class);
	}

	public String getCurrentBidPriceKey(long auctionId) {

		return String.format(CURRENT_BID_PRICE_KEY, auctionId);
	}

	public String getLastBidderIdKey(long auctionId) {

		return String.format(LAST_BIDDER_ID_KEY, auctionId);
	}

	public String getOpenSearchSyncFlagKey(long auctionId) {

		return String.format(OPENSEARCH_SYNC_FLAG_KEY, auctionId);
	}

}
