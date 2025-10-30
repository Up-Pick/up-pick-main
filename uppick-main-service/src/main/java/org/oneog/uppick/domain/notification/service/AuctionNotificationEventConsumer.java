package org.oneog.uppick.domain.notification.service;

import org.oneog.uppick.common.config.RabbitMQConfig;
import org.oneog.uppick.common.event.DomainEvent;
import org.oneog.uppick.domain.auction.event.BidPlacedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionNotificationEventConsumer {

    private final NotificationInternalService notificationInternalService;

    @RabbitListener(queues = RabbitMQConfig.AUCTION_NOTIFICATION_QUEUE)
    public void handleAuctionNotificationEvent(DomainEvent event) {

        switch (event) {
            case BidPlacedEvent bidPlacedEvent:
                notificationInternalService.sendNotification(bidPlacedEvent);
                break;

            default:
                log.warn("Unhandled event type: {}", event.getClass().getSimpleName());
                break;
        }
    }

}
