package org.oneog.uppick.product.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.oneog.uppick.product")
public class OpenFeignConfig {

}
