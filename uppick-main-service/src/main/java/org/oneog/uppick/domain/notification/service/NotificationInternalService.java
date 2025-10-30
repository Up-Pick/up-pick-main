package org.oneog.uppick.domain.notification.service;

import java.util.List;

import org.oneog.uppick.domain.auction.client.AuctionClient;
import org.oneog.uppick.domain.auction.event.BidPlacedEvent;
import org.oneog.uppick.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.entity.Notification;
import org.oneog.uppick.domain.notification.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationInternalService {

	private final NotificationJpaRepository notificationJpaRepository;
	private final NotificationMapper notificationMapper;
	private final AuctionClient auctionClient;

	@Transactional
	public void sendNotification(SendNotificationRequest request) {

		notificationJpaRepository.save(notificationMapper.toEntity(request));
	}

	@Transactional
	public void sendNotification(BidPlacedEvent event) {

		// 판매자 알림
		Notification notification = notificationMapper.toSellerNotification(event);
		notificationJpaRepository.save(notification);

		// 다른 입찰자에게 알림
		List<Long> bidderIds = auctionClient.getBiddingMemberIds(event.getAuctionId(), event.getBidderId());
		bidderIds.forEach(bidderId -> {
			Notification bidderNotification = notificationMapper.toBidderNotification(event);
			notificationJpaRepository.save(bidderNotification);
		});
	}

}