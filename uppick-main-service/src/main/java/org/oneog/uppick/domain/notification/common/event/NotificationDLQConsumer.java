package org.oneog.uppick.domain.notification.common.event;

import org.oneog.uppick.common.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationDLQConsumer {

	@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ_QUEUE)
	public void consumeMessage(Message message) {

		String eventType = (String)message.getMessageProperties().getHeaders().get("__TypeId__");
		String bodySummary = new String(message.getBody()).substring(0, Math.min(100, message.getBody().length));
		log.warn("DLQ 메시지 수신 - 이벤트 타입: {}, ID: {}, 헤더: {}, 본문 요약: {}, 프로퍼티: {}",
			eventType != null ? eventType : "알 수 없음",
			message.getMessageProperties().getMessageId(),
			message.getMessageProperties().getHeaders(),
			bodySummary,
			message.getMessageProperties());
	}

}
