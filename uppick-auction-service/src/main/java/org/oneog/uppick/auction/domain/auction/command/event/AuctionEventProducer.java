package org.oneog.uppick.auction.domain.auction.command.event;

import org.oneog.uppick.auction.common.config.RabbitMQConfig;
import org.oneog.uppick.common.event.DomainEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuctionEventProducer {

	public static final String AUCTION_EVENT_PREFIX = "auction.";
	private final RabbitTemplate rabbitTemplate;

	public void produce(AuctionEventType eventType, DomainEvent event) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.AUCTION_EXCHANGE_NAME, AUCTION_EVENT_PREFIX + eventType
			.asRoutingKey(), event);
	}

}
