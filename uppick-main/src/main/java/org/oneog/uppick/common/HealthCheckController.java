package org.oneog.uppick.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.oneog.uppickcommon.common.dto.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {
    @GetMapping("/ping")
    public GlobalApiResponse<String> pingPong() {
        return GlobalApiResponse.ok("pong");
    }
}
