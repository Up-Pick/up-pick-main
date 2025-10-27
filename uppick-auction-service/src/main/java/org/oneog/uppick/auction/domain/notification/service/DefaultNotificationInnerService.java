package org.oneog.uppick.auction.domain.notification.service;

import org.oneog.uppick.auction.domain.notification.client.NotificationClient;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultNotificationInnerService implements NotificationInnerService {

	private final NotificationClient notificationClient;

	public void sendNotification(SendNotificationRequest request) {

		notificationClient.sendNotification(request);
	}

}
