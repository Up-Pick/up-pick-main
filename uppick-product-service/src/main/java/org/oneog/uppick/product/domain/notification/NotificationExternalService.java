package org.oneog.uppick.product.domain.notification;

import org.oneog.uppick.product.domain.notification.enums.NotificationType;

public interface NotificationExternalService {

	void sendNotification(Long sellerId, NotificationType bid, String string, String string2);

}
