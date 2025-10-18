package org.oneog.uppick.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.notification.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUnreadNotificationsResponse {
    private final List<NotificationDetail> notifications;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class NotificationDetail {
        private final NotificationType type;
        private final String title;
        private final String message;
        private final LocalDateTime notifiedAt;
    }
}
