package org.oneog.uppick.domain.notification.service;

import org.oneog.uppick.domain.notification.dto.request.SendNotificationRequest;
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
	public void sendNotification(SendNotificationRequest request) {

		notificationJpaRepository.save(notificationMapper.toEntity(request));
	}

}