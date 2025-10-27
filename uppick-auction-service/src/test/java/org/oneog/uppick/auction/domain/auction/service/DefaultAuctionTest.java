package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.auction.entity.Auction;
import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.repository.AuctionRepository;

@ExtendWith(MockitoExtension.class)
public class DefaultAuctionTest {

	@Mock
	AuctionRepository auctionRepository;

	@InjectMocks
	DefaultAuctionInnerService defaultAuctionInnerService;

	@Test
	void registerAuction_호출시_save정상호출() {
		// given
		Long id = 1L;
		Long registerId = 100L;
		Long startBid = 5000L;
		LocalDateTime registeredAt = LocalDateTime.now();
		LocalDateTime endAt = registeredAt.plusDays(1);

		//when
		defaultAuctionInnerService.registerAuction(id, registerId, startBid, registeredAt, endAt);

		// then
		ArgumentCaptor<Auction> captor = ArgumentCaptor.forClass(Auction.class);
		verify(auctionRepository, times(1)).save(captor.capture());

		Auction savedAuction = captor.getValue();

		assertThat(savedAuction.getProductId()).isEqualTo(id);
		assertThat(savedAuction.getRegisterId()).isEqualTo(registerId);
		assertThat(savedAuction.getMinPrice()).isEqualTo(startBid);
		assertThat(savedAuction.getStartAt()).isEqualTo(registeredAt);
		assertThat(savedAuction.getEndAt()).isEqualTo(endAt);
		assertThat(savedAuction.getStatus()).isEqualTo(AuctionStatus.IN_PROGRESS);
	}

}
