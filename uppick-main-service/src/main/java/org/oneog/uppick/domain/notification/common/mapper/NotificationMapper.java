package org.oneog.uppick.domain.notification.common.mapper;

import java.util.List;

import org.oneog.uppick.domain.auction.event.BidPlacedEvent;
import org.oneog.uppick.domain.notification.command.model.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.query.model.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.command.entity.Notification;
import org.oneog.uppick.domain.notification.command.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

	public GetUnreadNotificationsResponse toResponse(List<Notification> notifications) {

		List<GetUnreadNotificationsResponse.NotificationDetail> notificationDetails = notifications
			.stream()
			.map(notification ->
				GetUnreadNotificationsResponse.NotificationDetail.builder()
					.type(notification.getType())
					.title(notification.getTitle())
					.message(notification.getMessage())
					.notifiedAt(notification.getNotifiedAt())
					.build())
			.toList();

		return new GetUnreadNotificationsResponse(notificationDetails);
	}

	public Notification toEntity(SendNotificationRequest request) {

		return Notification.builder()
			.memberId(request.getMemberId())
			.type(request.getType())
			.title(request.getString1())
			.message(request.getString2())
			.build();
	}

	public Notification toSellerNotification(BidPlacedEvent event) {

		return Notification.builder()
			.memberId(event.getSellerId())
			.type(NotificationType.BID)
			.title("현재 입찰가가 갱신되었습니다!")
			.message("현재 입찰가: " + event.getBiddingPrice())
			.build();
	}

	public Notification toBidderNotification(BidPlacedEvent event, long bidderId) {

		return Notification.builder()
			.memberId(bidderId)
			.type(NotificationType.BID)
			.title("현재 입찰가가 갱신되었습니다!")
			.message("현재 입찰가: " + event.getBiddingPrice())
			.build();
	}

}
