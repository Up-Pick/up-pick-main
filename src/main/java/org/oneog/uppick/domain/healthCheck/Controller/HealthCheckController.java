package org.oneog.uppick.domain.healthCheck.Controller;

import java.util.List;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping("/health")
	public GlobalApiResponse<?> healthCheck() {
		String status = "UP";
		return GlobalApiResponse.ok(status);
	}

	@GetMapping("/health2")
	public GlobalApiResponse<?> healthCheck2() {
		List<String> healthStatus = List.of("서버 정상", "DB 연결 OK", "캐시 정상");
		return GlobalApiResponse.ok(healthStatus);
	}

}
