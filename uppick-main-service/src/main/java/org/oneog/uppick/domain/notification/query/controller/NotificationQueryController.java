package org.oneog.uppick.domain.notification.query.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.notification.query.service.NotificationQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;

    // 사용자가 읽지 않은 알림의 개수를 가져오는 엔드포인트
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread/count/me")
    public GlobalApiResponse<Long> countUnreadNotificationsByMemberId(@AuthenticationPrincipal AuthMember authMember) {

        long unreadCount = notificationQueryService.countUnreadNotificationsByMemberId(authMember.getMemberId());
        return GlobalApiResponse.ok(unreadCount);
    }

}
