package org.oneog.uppick.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.oneog.uppick")
public class OpenFeignConfig {

}
