package org.oneog.uppick.product.domain.notification.service;

import org.oneog.uppick.product.domain.notification.dto.request.SendNotificationRequest;

public interface NotificationInnerService {

	void sendNotification(SendNotificationRequest request);

}
