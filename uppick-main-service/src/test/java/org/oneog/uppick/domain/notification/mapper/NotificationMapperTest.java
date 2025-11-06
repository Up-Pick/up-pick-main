package org.oneog.uppick.domain.notification.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.notification.command.entity.Notification;
import org.oneog.uppick.domain.notification.command.entity.NotificationType;
import org.oneog.uppick.domain.notification.query.model.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.common.mapper.NotificationMapper;

@ExtendWith(MockitoExtension.class)
public class NotificationMapperTest {

    private NotificationMapper notificationMapper;

    @Mock
    private Notification notification1;

    @Mock
    private Notification notification2;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapper();
    }

    @Test
    void toResponse_빈리스트가주어지면_빈응답을반환한다() {
        // Given
        List<Notification> notifications = List.of();

        // When
        GetUnreadNotificationsResponse response = notificationMapper.toResponse(notifications);

        // Then
        assertTrue(response.getNotifications().isEmpty());
    }

    @Test
    void toResponse_알림리스트가주어지면_응답을반환한다() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        when(notification1.getType()).thenReturn(NotificationType.TRADE);
        when(notification1.getTitle()).thenReturn("Trade Notification");
        when(notification1.getMessage()).thenReturn("Your trade has been completed.");
        when(notification1.getNotifiedAt()).thenReturn(now);

        when(notification2.getType()).thenReturn(NotificationType.BID);
        when(notification2.getTitle()).thenReturn("Bid Notification");
        when(notification2.getMessage()).thenReturn("You have a new bid.");
        when(notification2.getNotifiedAt()).thenReturn(now.minusHours(1));

        List<Notification> notifications = List.of(notification1, notification2);

        // When
        GetUnreadNotificationsResponse response = notificationMapper.toResponse(notifications);

        // Then
        assertEquals(2, response.getNotifications().size());

        GetUnreadNotificationsResponse.NotificationDetail detail1 = response.getNotifications().get(0);
        assertEquals(NotificationType.TRADE, detail1.getType());
        assertEquals("Trade Notification", detail1.getTitle());
        assertEquals("Your trade has been completed.", detail1.getMessage());
        assertEquals(now, detail1.getNotifiedAt());

        GetUnreadNotificationsResponse.NotificationDetail detail2 = response.getNotifications().get(1);
        assertEquals(NotificationType.BID, detail2.getType());
        assertEquals("Bid Notification", detail2.getTitle());
        assertEquals("You have a new bid.", detail2.getMessage());
        assertEquals(now.minusHours(1), detail2.getNotifiedAt());
    }
}
