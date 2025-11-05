package org.oneog.uppick.domain.notification.command.service;

import java.util.List;

import org.oneog.uppick.domain.notification.query.model.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.command.entity.Notification;
import org.oneog.uppick.domain.notification.common.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.command.repository.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

	private final NotificationJpaRepository notificationJpaRepository;
	private final NotificationMapper notificationMapper;

	@Transactional
	public GetUnreadNotificationsResponse getUnreadNotifications(long memberId) {

		log.debug("NotificationCommandService - 읽지 않은 알림 조회 시도 ⏳");

		// 읽음 처리
		List<Notification> unreadNotifications = notificationJpaRepository.findAllByMemberIdAndIsReadFalse(memberId);
		unreadNotifications.forEach(Notification::markAsRead);

		GetUnreadNotificationsResponse response = notificationMapper.toResponse(unreadNotifications);

		log.debug("NotificationCommandService - 읽지 않은 알림 조회 성공 ✅");

		return response;
	}

}
