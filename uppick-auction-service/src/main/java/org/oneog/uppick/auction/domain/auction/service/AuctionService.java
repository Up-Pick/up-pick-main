package org.oneog.uppick.auction.domain.auction.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.oneog.uppick.auction.common.lock.LockManager;
import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.event.AuctionEventType;
import org.oneog.uppick.auction.domain.auction.event.BidPlacedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

	private static final String BIDDING_LOCK_KEY_PREFIX = "auction:bidding:";

	// ***** Auction Domain ***** //
	private final BiddingProcessor biddingProcessor;

	// ****** Inner Service ***** //
	private final LockManager lockManager;

	private final AuctionEventProducer auctionEventProducer;

	// 특정 상품에 입찰 시도를 한다
	@Transactional(readOnly = false)
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {

		String lockKey = BIDDING_LOCK_KEY_PREFIX + auctionId;
		BiddingResultDto result = lockManager.executeWithLock(lockKey, () -> biddingProcessor.process(request,
			auctionId, memberId));

		BidPlacedEvent bidPlacedEvent = BidPlacedEvent.builder()
			.sellerId(result.getSellerId())
			.bidderId(memberId)
			.auctionId(auctionId)
			.biddingPrice(result.getBiddingPrice())
			.eventId(UUID.randomUUID().toString() + "-" + auctionId)
			.occurredAt(LocalDateTime.now())
			.build();
		auctionEventProducer.produce(AuctionEventType.BID_PLACED, bidPlacedEvent);
	}

}
