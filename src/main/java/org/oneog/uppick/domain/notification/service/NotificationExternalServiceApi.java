package org.oneog.uppick.domain.notification.service;

import org.oneog.uppick.domain.notification.entity.NotificationType;

public interface NotificationExternalServiceApi {

	/**
	 * 알림을 발생시킬 때 사용하는 API.
	 * 예: 입찰 발생, 낙찰 확정, 경매 만료 등
	 *
	 * @param receiverId 알림을 받을 회원 ID
	 * @param type 알림 타입
	 * @param title 알림 제목
	 * @param message 알림 내용
	 */
	default void sendNotification(Long receiverId, NotificationType type, String title, String message) {
		return;
	}

	;
}
