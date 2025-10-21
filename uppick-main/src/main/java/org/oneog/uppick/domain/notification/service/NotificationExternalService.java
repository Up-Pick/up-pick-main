package org.oneog.uppick.domain.notification.service;

import org.oneog.uppick.domain.notification.entity.Notification;
import org.oneog.uppick.domain.notification.entity.NotificationType;
import org.oneog.uppick.domain.notification.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationExternalService implements NotificationExternalServiceApi {
    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    @Transactional
    public void sendNotification(Long receiverId, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
            .memberId(receiverId)
            .type(type)
            .title(title)
            .message(message)
            .build();
        notificationJpaRepository.save(notification);
    }
}
