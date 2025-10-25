package org.oneog.uppick.domain.notification.service;

import java.util.List;

import org.oneog.uppick.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.entity.Notification;
import org.oneog.uppick.domain.notification.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationInternalService {
	private final NotificationJpaRepository notificationJpaRepository;
	private final NotificationMapper notificationMapper;

	@Transactional
	public GetUnreadNotificationsResponse getUnreadNotifications(long memberId) {
		List<Notification> unreadNotifications = notificationJpaRepository.findAllByMemberIdAndIsReadFalse(memberId);

		// 읽음 처리
		unreadNotifications.forEach(notification -> {
			notification.markAsRead();
		});

		return notificationMapper.toResponse(unreadNotifications);
	}

	@Transactional
	public void sendNotification(SendNotificationRequest request) {
		Notification notification = Notification.builder()
			.memberId(request.getMemberId())
			.type(request.getType())
			.title(request.getString1())
			.message(request.getString1())
			.build();
		notificationJpaRepository.save(notification);
	}
}
