package org.oneog.uppick.domain.notification.command.controller.internal;

import org.oneog.uppick.domain.notification.command.model.dto.request.SendNotificationRequest;
import org.oneog.uppick.domain.notification.command.service.NotificationInternalCommandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class NotificationInternalCommandController {

	private final NotificationInternalCommandService notificationInternalCommandService;

	@PostMapping("/notifications")
	public void sendNotification(@RequestBody SendNotificationRequest request) {

		notificationInternalCommandService.sendNotification(request);
	}

}
