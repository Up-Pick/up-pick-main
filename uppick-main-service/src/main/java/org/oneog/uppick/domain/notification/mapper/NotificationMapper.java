package org.oneog.uppick.domain.notification.mapper;

import java.util.List;

import org.oneog.uppick.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
	public GetUnreadNotificationsResponse toResponse(List<Notification> notifications) {
		List<GetUnreadNotificationsResponse.NotificationDetail> notificationDetails = notifications.stream()
			.map(notification -> {
				return GetUnreadNotificationsResponse.NotificationDetail.builder()
					.type(notification.getType())
					.title(notification.getTitle())
					.message(notification.getMessage())
					.notifiedAt(notification.getNotifiedAt())
					.build();
			}).toList();

		return new GetUnreadNotificationsResponse(notificationDetails);
	}

	public Notification toEntity(SendNotificationRequest request) {
		Notification notification = Notification.builder()
			.memberId(request.getMemberId())
			.type(request.getType())
			.title(request.getString1())
			.message(request.getString2())
			.build();
		return notification;
	}

}
