package org.oneog.uppick.domain.notification.query.service;

import org.oneog.uppick.domain.notification.query.repository.NotificationQueryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationQueryRepository notificationQueryRepository;

    public long countUnreadNotificationsByMemberId(Long memberId) {

        return notificationQueryRepository.countUnreadNotificationsByMemberId(memberId);
    }

}
