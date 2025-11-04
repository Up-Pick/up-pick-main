package org.oneog.uppick.auction.domain.auction.command.event;

import java.time.LocalDateTime;

import org.oneog.uppick.auction.common.config.RabbitMQConfig;
import org.oneog.uppick.auction.domain.auction.command.service.AuctionEndProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventConsumer {

	private final AuctionEndProcessor auctionEndProcessor;

	@RabbitListener(queues = RabbitMQConfig.AUCTION_ENDED_QUEUE)
	public void handleAuctionEndedEvent(AuctionEndedEvent event) {

		log.info("Auction ended #{} on {}", event.getAuctionId(), LocalDateTime.now().toString());
		// 경매 종료 이벤트 처리 로직 구현
		auctionEndProcessor.process(event.getAuctionId());
	}

}
