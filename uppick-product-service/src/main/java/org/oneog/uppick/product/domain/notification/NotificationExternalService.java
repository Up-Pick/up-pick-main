package org.oneog.uppick.product.domain.notification;

public interface NotificationExternalService {

    void sendNotification(Long sellerId, NotificationType bid, String string, String string2);

}
