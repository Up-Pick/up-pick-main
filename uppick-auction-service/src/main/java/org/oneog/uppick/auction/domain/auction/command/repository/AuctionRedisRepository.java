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

	/**
	 * OpenSearch 동기화 플래그 생성
	 * Lambda가 이 플래그를 감지하여 OpenSearch에 입찰가 업데이트
	 */
	public void createOpenSearchSyncFlag(long auctionId, long bidPrice) {

		String syncFlagKey = auctionRedisConstant.getOpenSearchSyncFlagKey(auctionId);
		stringRedisTemplate.opsForValue().set(syncFlagKey, String.valueOf(bidPrice));
	}

	/**
	 * 경매 종료 시 Redis 키 정리
	 * - auction:{auctionId}:current-bid-price
	 * - auction:{auctionId}:last-bidder-id
	 */
	public void deleteAuctionKeys(long auctionId) {

		String currentBidPriceKey = auctionRedisConstant.getCurrentBidPriceKey(auctionId);
		String lastBidderIdKey = auctionRedisConstant.getLastBidderIdKey(auctionId);
		stringRedisTemplate.delete(Arrays.asList(currentBidPriceKey, lastBidderIdKey));
	}

}
