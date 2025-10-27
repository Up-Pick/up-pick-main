package org.oneog.uppick.product.domain.notification.service;

import org.oneog.uppick.product.domain.notification.client.NotificationClient;
import org.oneog.uppick.product.domain.notification.dto.request.SendNotificationRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendNotificationUseCase {

	private final NotificationClient notificationClient;

	public void execute(SendNotificationRequest request) {
		notificationClient.sendNotification(request);
	}
}
