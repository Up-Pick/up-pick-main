package org.oneog.uppick.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;

@ExtendWith(MockitoExtension.class)
public class AuctionInternalServiceTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private AuctionQueryRepository auctionQueryRepository;
	@Mock
	private AuctionMapper auctionMapper;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;

	@InjectMocks
	private AuctionInternalService auctionInternalService;

	@Test
	void bid_입찰시도_성공() {
		// given
		long auctionId = 1L;
		long memberId = 100L;
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.status(Status.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

		BiddingDetail biddingDetail = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(newBidPrice)
			.build();

		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
			.willReturn(biddingDetail);

		// when
		auctionInternalService.bid(request, auctionId, memberId);

		// then
		assertThat(auction.getCurrentPrice()).isEqualTo(newBidPrice);
		then(biddingDetailRepository).should().save(any(BiddingDetail.class));
	}

	@Test
	void bid_잘못된입찰가_실패() {
		// given
		long auctionId = 1L;
		long memberId = 100L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(2000L)
			.status(Status.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(1500L); // 현재입찰가(2000)보다 낮음

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

		// when/then
		assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessage(AuctionErrorCode.WRONG_BIDDING_PRICE.getMessage());

	}
}
