package org.oneog.uppick.domain.notification.command.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.notification.query.model.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.command.service.NotificationCommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationCommandController {

	private final NotificationCommandService notificationCommandService;

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public GlobalApiResponse<GetUnreadNotificationsResponse> getUnreadNotifications(
		@AuthenticationPrincipal AuthMember authMember) {

		return GlobalApiResponse.ok(notificationCommandService.getUnreadNotifications(authMember.getMemberId()));
	}

}
