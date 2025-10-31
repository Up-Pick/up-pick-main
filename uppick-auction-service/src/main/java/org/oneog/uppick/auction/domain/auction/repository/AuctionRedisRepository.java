package org.oneog.uppick.auction.domain.auction.repository;

import java.util.Arrays;

import org.oneog.uppick.auction.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.auction.domain.auction.redis.AuctionRedisConstant;
import org.oneog.uppick.common.exception.BusinessException;
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
        return Long.parseLong(bidPrice);
    }

    public Long findLastBidderId(long auctionId) {

        String bidderId = stringRedisTemplate.opsForValue().get(auctionRedisConstant.getLastBidderIdKey(auctionId));
        return bidderId != null ? Long.parseLong(bidderId) : null;
    }

    public void updateBidStatus(long auctionId, long bidPrice, long bidderId) {

        String currentBidPriceKey = auctionRedisConstant.getCurrentBidPriceKey(auctionId);
        String lastBidderIdKey = auctionRedisConstant.getLastBidderIdKey(auctionId);
        Long result = stringRedisTemplate.execute(auctionRedisConstant.biddingScript,
            Arrays.asList(currentBidPriceKey, lastBidderIdKey),
            String.valueOf(bidPrice), String.valueOf(bidderId));
        if (result == null || result == 0L) {
            throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
        }
    }

}
