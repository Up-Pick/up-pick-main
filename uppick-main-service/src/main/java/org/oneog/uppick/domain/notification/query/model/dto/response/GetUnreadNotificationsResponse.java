package org.oneog.uppick.domain.notification.query.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.notification.command.entity.NotificationType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GetUnreadNotificationsResponse {

	private List<NotificationDetail> notifications;

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