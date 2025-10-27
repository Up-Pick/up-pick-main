package org.oneog.uppick.domain.notification.dto.request;

import org.oneog.uppick.domain.notification.entity.NotificationType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SendNotificationRequest {

	private Long memberId;
	private NotificationType type;
	private String string1;
	private String string2;

}