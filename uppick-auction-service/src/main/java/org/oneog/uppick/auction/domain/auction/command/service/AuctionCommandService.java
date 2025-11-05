package org.oneog.uppick.auction.domain.auction.command.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.exception.LockAcquisitionException;
import org.oneog.uppick.auction.common.lock.LockManager;
import org.oneog.uppick.auction.domain.auction.command.event.AuctionEventProducer;
import org.oneog.uppick.auction.domain.auction.command.event.AuctionEventType;
import org.oneog.uppick.auction.domain.auction.command.event.BidPlacedEvent;
import org.oneog.uppick.auction.domain.auction.command.model.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.command.model.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.command.service.component.BiddingProcessor;
import org.oneog.uppick.auction.domain.auction.common.exception.AuctionErrorCode;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionCommandService {

	private static final String BIDDING_LOCK_KEY_PREFIX = "auction:bidding:";

	// ***** Auction Domain ***** //
	private final BiddingProcessor biddingProcessor;

	// ****** Inner Service ***** //
	private final LockManager lockManager;

	private final AuctionEventProducer auctionEventProducer;

	// 특정 상품에 입찰 시도를 한다
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {

		String lockKey = BIDDING_LOCK_KEY_PREFIX + auctionId;
		BiddingResultDto result;

		try {
			result = lockManager.executeWithLock(lockKey, () -> biddingProcessor.process(request, auctionId, memberId));
		} catch (LockAcquisitionException e) {
			log.error("입찰 처리 중 락 획득 실패", e);
			throw new BusinessException(AuctionErrorCode.BID_FAILED_CAUSE_ACQUIRE_LOCK_FAILED);
		} catch (Exception e) {
			log.error("입찰 처리 중 예외 발생", e);
			throw e;
		}

		BidPlacedEvent bidPlacedEvent = BidPlacedEvent.builder()
			.sellerId(result.getSellerId())
			.bidderId(memberId)
			.auctionId(auctionId)
			.biddingPrice(result.getBiddingPrice())
			.eventId(UUID.randomUUID() + "-" + auctionId)
			.occurredAt(LocalDateTime.now())
			.build();
		auctionEventProducer.produce(AuctionEventType.BID_PLACED, bidPlacedEvent);
	}

}
