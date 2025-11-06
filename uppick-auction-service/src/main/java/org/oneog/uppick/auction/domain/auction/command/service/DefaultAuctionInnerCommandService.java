package org.oneog.uppick.auction.domain.auction.command.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.oneog.uppick.auction.common.config.RabbitMQConfig;
import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.event.AuctionEndedEvent;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultAuctionInnerCommandService implements AuctionInnerCommandService {

	private final AuctionRepository auctionRepository;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	public void registerAuction(Long id, Long registerId, Long startBid, LocalDateTime registeredAt,
		LocalDateTime endAt) {

		Auction auction = Auction.builder()
			.productId(id)
			.registerId(registerId)
			.minPrice(startBid)
			.startAt(registeredAt)
			.endAt(endAt)
			.status(AuctionStatus.IN_PROGRESS)
			.build();
		auctionRepository.save(auction);

		AuctionEndedEvent auctionEndedEvent = AuctionEndedEvent.builder()
			.auctionId(auction.getId())
			.eventId(UUID.randomUUID() + "-" + "auctionId")
			.occurredAt(LocalDateTime.now())
			.build();

		rabbitTemplate.convertAndSend(RabbitMQConfig.AUCTION_ENDED_EXCHANGE_NAME,
			RabbitMQConfig.AUCTION_ENDED_ROUTING_KEY,
			auctionEndedEvent, msg -> {
				long delay = Duration.between(LocalDateTime.now(), endAt.plus(1L, ChronoUnit.MILLIS)).toMillis();
				msg.getMessageProperties().setDelayLong(delay);
				return msg;
			});
	}

}
