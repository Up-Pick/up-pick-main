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
import org.oneog.uppick.auction.common.config.RabbitMQConfig;
import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.event.AuctionEndedEvent;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.command.service.DefaultAuctionInnerCommandService;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DefaultAuctionTest {

	@Mock
	AuctionRepository auctionRepository;

	@Mock
	RabbitTemplate rabbitTemplate;

	@InjectMocks
	DefaultAuctionInnerCommandService defaultAuctionInnerService;

	@Test
	void registerAuction_호출시_save정상호출() {

		// given
		Long id = 1L;
		Long registerId = 100L;
		Long startBid = 5000L;
		LocalDateTime registeredAt = LocalDateTime.now();
		LocalDateTime endAt = registeredAt.plusDays(1);

		when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> {
			Auction auction = invocation.getArgument(0);
			ReflectionTestUtils.setField(auction, "id", 1L); // ID 설정
			return auction;
		});

		//when
		defaultAuctionInnerService.registerAuction(id, registerId, startBid, registeredAt, endAt);

		// then
		ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
		verify(auctionRepository, times(1)).save(auctionCaptor.capture());

		Auction savedAuction = auctionCaptor.getValue();

		assertThat(savedAuction.getProductId()).isEqualTo(id);
		assertThat(savedAuction.getRegisterId()).isEqualTo(registerId);
		assertThat(savedAuction.getMinPrice()).isEqualTo(startBid);
		assertThat(savedAuction.getStartAt()).isEqualTo(registeredAt);
		assertThat(savedAuction.getEndAt()).isEqualTo(endAt);
		assertThat(savedAuction.getStatus()).isEqualTo(AuctionStatus.IN_PROGRESS);
		assertThat(savedAuction.getId()).isEqualTo(1L); // ID 검증 추가

		// 이벤트 전송 검증
		ArgumentCaptor<AuctionEndedEvent> eventCaptor = ArgumentCaptor.forClass(AuctionEndedEvent.class);
		verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.AUCTION_ENDED_EXCHANGE_NAME), eq(
			RabbitMQConfig.AUCTION_ENDED_ROUTING_KEY), eventCaptor.capture(), any(MessagePostProcessor.class));

		AuctionEndedEvent sentEvent = eventCaptor.getValue();
		assertThat(sentEvent.getAuctionId()).isEqualTo(savedAuction.getId());
		assertThat(sentEvent.getEventId()).isNotNull();
		assertThat(sentEvent.getOccurredAt()).isNotNull();
	}

}
