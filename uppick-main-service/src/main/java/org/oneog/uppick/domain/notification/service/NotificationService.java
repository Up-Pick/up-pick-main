package org.oneog.uppick.domain.notification.service;

import java.util.List;

import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.entity.Notification;
import org.oneog.uppick.domain.notification.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationJpaRepository notificationJpaRepository;
	private final NotificationMapper notificationMapper;

	@Transactional
	public GetUnreadNotificationsResponse getUnreadNotifications(long memberId) {

		log.info("NotificationService.getUnreadNotifications : 읽지 않은 알림 조회 시도");

		// 읽음 처리
		List<Notification> unreadNotifications = notificationJpaRepository.findAllByMemberIdAndIsReadFalse(memberId);
		unreadNotifications.forEach(Notification::markAsRead);

		log.info("NotificationService.getUnreadNotifications : 읽지 않은 알림 조회 성공 ✅");

		return notificationMapper.toResponse(unreadNotifications);
	}

}