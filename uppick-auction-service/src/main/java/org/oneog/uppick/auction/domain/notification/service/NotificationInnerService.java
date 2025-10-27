package org.oneog.uppick.auction.domain.notification.service;

import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;

public interface NotificationInnerService {

	void sendNotification(SendNotificationRequest request);

}
