package org.oneog.uppick.batch.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.oneog.uppick.batch")
public class OpenFeignConfig {

}
