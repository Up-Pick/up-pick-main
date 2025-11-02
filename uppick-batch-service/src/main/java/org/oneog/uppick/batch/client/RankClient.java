package org.oneog.uppick.batch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "main-service", url = "${MAIN_SERVICE_URL}")
public interface RankClient {

	@PostMapping("/internal/v1/rankings/update-hot-keywords")
	void updateHotKeywords();

}
