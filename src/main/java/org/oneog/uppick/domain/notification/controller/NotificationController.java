package org.oneog.uppick.domain.notification.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.service.NotificationInternalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationInternalService notificationInternalService;

    @GetMapping("/me")
    public GlobalApiResponse<GetUnreadNotificationsResponse> getUnreadNotifications(@AuthenticationPrincipal
    AuthMember authMember) {
        return GlobalApiResponse.ok(notificationInternalService.getUnreadNotifications(authMember.getMemberId()));
    }
}
