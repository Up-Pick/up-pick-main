package org.oneog.uppick.domain.notification.controller;

import org.oneog.uppick.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.service.NotificationInternalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class NotificationInternalController {

	private final NotificationInternalService notificationInternalService;

	@PostMapping("/notifications")
	public void sendNotification(@RequestBody SendNotificationRequest request) {

		notificationInternalService.sendNotification(request);
	}

}