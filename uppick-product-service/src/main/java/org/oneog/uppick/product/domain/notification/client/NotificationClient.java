package org.oneog.uppick.product.domain.notification.client;

import org.oneog.uppick.product.domain.notification.dto.request.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-client", url = "${gateway.url}")
public interface NotificationClient {

	@PostMapping("/main/internal/v1/notifications")
	void sendNotification(@RequestBody SendNotificationRequest request);
}
