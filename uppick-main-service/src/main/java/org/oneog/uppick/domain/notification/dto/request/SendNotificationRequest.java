package org.oneog.uppick.domain.notification.dto.request;

import org.oneog.uppick.domain.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendNotificationRequest {
	private Long memberId;
	private NotificationType type;
	private String string1;
	private String string2;
}
