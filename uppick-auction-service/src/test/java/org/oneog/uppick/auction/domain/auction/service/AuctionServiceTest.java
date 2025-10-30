package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.common.lock.LockManager;
import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.event.AuctionEventType;
import org.oneog.uppick.auction.domain.auction.event.BidPlacedEvent;
import org.oneog.uppick.auction.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AuctionServiceTest {

	@Mock
	private BiddingProcessor biddingProcessor;

	@Mock
	private LockManager lockManager;

	@Mock
	private AuctionEventProducer auctionEventProducer;

	@InjectMocks
	private AuctionService auctionService;

	@Test
	void bid_정상적인상황_성공함() {

		// Given
		AuctionBidRequest request = mock(AuctionBidRequest.class);
		long auctionId = 1L;
		long memberId = 2L;
		BiddingResultDto result = new BiddingResultDto(3L, 1000L);

		given(lockManager.executeWithLock(anyString(), any(Supplier.class))).willReturn(result);

		// When
		auctionService.bid(request, auctionId, memberId);

		// Then
		then(lockManager).should().executeWithLock(eq("auction:bidding:1"), any(Supplier.class));
		then(auctionEventProducer).should().produce(eq(AuctionEventType.BID_PLACED), any(BidPlacedEvent.class));
	}

	@Test
	void bid_예외발생시_이벤트생성안함() {

		// Given
		AuctionBidRequest request = mock(AuctionBidRequest.class);
		long auctionId = 1L;
		long memberId = 2L;

		given(lockManager.executeWithLock(anyString(), any(Supplier.class))).willThrow(new BusinessException(
			AuctionErrorCode.AUCTION_NOT_FOUND));

		// When & Then
		assertThatThrownBy(() -> auctionService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class);

		then(auctionEventProducer).should(never()).produce(any(), any());
	}

}
