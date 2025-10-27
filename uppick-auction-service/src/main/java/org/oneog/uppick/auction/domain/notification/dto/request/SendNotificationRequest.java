package org.oneog.uppick.auction.domain.notification.dto.request;

import org.oneog.uppick.auction.domain.notification.enums.NotificationType;

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
