package org.oneog.uppick.auction.domain.auction.command.repository;

import java.util.Arrays;

import org.oneog.uppick.auction.domain.auction.common.redis.AuctionRedisConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuctionRedisRepository {

	private final StringRedisTemplate stringRedisTemplate;
	private final AuctionRedisConstant auctionRedisConstant;

	public Long findCurrentBidPrice(long auctionId) {

		String bidPrice = stringRedisTemplate.opsForValue().get(auctionRedisConstant.getCurrentBidPriceKey(auctionId));
		return bidPrice != null ? Long.parseLong(bidPrice) : null;
	}

	public Long findLastBidderId(long auctionId) {

		String bidderId = stringRedisTemplate.opsForValue().get(auctionRedisConstant.getLastBidderIdKey(auctionId));
		return bidderId != null ? Long.parseLong(bidderId) : null;
	}

	public void updateBidStatus(long auctionId, long bidPrice, long bidderId) {

		String currentBidPriceKey = auctionRedisConstant.getCurrentBidPriceKey(auctionId);
		String lastBidderIdKey = auctionRedisConstant.getLastBidderIdKey(auctionId);
		stringRedisTemplate.execute(auctionRedisConstant.biddingScript,
			Arrays.asList(currentBidPriceKey, lastBidderIdKey),
			String.valueOf(bidPrice), String.valueOf(bidderId));
	}

}
