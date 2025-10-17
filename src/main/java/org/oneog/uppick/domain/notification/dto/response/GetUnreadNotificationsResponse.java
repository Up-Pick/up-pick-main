package org.oneog.uppick.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.common.constants.ResponseConstants;
import org.oneog.uppick.domain.notification.entity.NotificationType;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ResponseConstants.JSON_DATE_FORMAT)
        private LocalDateTime notifiedAt;
    }
}
