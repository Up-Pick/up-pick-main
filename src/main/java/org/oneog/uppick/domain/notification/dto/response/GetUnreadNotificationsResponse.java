package org.oneog.uppick.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.notification.entity.NotificationType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GetUnreadNotificationsResponse {
    List<NotificationDetail> notifications;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class NotificationDetail {
        private NotificationType type;
        private String title;
        private String message;
        private LocalDateTime notifiedAt;
    }
}
