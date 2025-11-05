package org.oneog.uppick.domain.notification.command.service;

import java.util.List;

import org.oneog.uppick.domain.auction.client.AuctionClient;
import org.oneog.uppick.domain.auction.event.BidPlacedEvent;
import org.oneog.uppick.domain.notification.command.model.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.command.entity.Notification;
import org.oneog.uppick.domain.notification.common.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.command.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInternalCommandService {

	private final NotificationJpaRepository notificationJpaRepository;
	private final NotificationMapper notificationMapper;
	private final AuctionClient auctionClient;

	@Transactional
	public void sendNotification(SendNotificationRequest request) {

		log.debug("NotificationInternalCommandService - 알림 데이터 저장 시도 ⏳");

		notificationJpaRepository.save(notificationMapper.toEntity(request));

		log.debug("NotificationInternalCommandService - 알림 데이터 저장 성공 ✅");
	}

	@Transactional
	public void sendNotification(BidPlacedEvent event) {

		log.debug("NotificationInternalCommandService - 알림 시도 ⏳");

		// 판매자 알림
		log.debug("-- 판매자 알림 생성");
		Notification notification = notificationMapper.toSellerNotification(event);
		notificationJpaRepository.save(notification);

		// 다른 입찰자에게 알림
		log.debug("-- 입찰자 알림 생성");
		List<Long> bidderIds = auctionClient.getBiddingMemberIds(event.getAuctionId(), event.getBidderId());
		bidderIds.forEach(bidderId -> {
			Notification bidderNotification = notificationMapper.toBidderNotification(event, bidderId);
			notificationJpaRepository.save(bidderNotification);
		});

		log.debug("NotificationInternalCommandService - 알림 성공 ✅");
	}

}
