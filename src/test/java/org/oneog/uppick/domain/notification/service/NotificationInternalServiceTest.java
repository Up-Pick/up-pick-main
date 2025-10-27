package org.oneog.uppick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.entity.Notification;
import org.oneog.uppick.domain.notification.mapper.NotificationMapper;
import org.oneog.uppick.domain.notification.repository.NotificationJpaRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationInternalServiceTest {

    @InjectMocks
    private NotificationInternalService notificationInternalService;

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private Notification notification1;

    @Mock
    private Notification notification2;

    @Test
    void getUnreadNotifications_읽지않은알림존재_읽지않은알림반환및읽음표시() {
        // given
        long memberId = 1L;
        List<Notification> unreadNotifications = List.of(notification1, notification2);
        GetUnreadNotificationsResponse expectedResponse = new GetUnreadNotificationsResponse(Collections.emptyList());

        when(notificationJpaRepository.findAllByMemberIdAndIsReadFalse(memberId)).thenReturn(unreadNotifications);
        when(notificationMapper.toResponse(unreadNotifications)).thenReturn(expectedResponse);

        // when
        GetUnreadNotificationsResponse result = notificationInternalService.getUnreadNotifications(memberId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(notificationJpaRepository).findAllByMemberIdAndIsReadFalse(memberId);
        verify(notification1).markAsRead();
        verify(notification2).markAsRead();
        verify(notificationMapper).toResponse(unreadNotifications);
    }

    @Test
    void getUnreadNotifications_읽지않은알림없음_빈리스트반환() {
        // given
        long memberId = 1L;
        List<Notification> unreadNotifications = Collections.emptyList();
        GetUnreadNotificationsResponse expectedResponse = new GetUnreadNotificationsResponse(Collections.emptyList());

        when(notificationJpaRepository.findAllByMemberIdAndIsReadFalse(memberId)).thenReturn(unreadNotifications);
        when(notificationMapper.toResponse(unreadNotifications)).thenReturn(expectedResponse);

        // when
        GetUnreadNotificationsResponse result = notificationInternalService.getUnreadNotifications(memberId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(notificationJpaRepository).findAllByMemberIdAndIsReadFalse(memberId);
        verify(notificationMapper).toResponse(unreadNotifications);
    }
}
