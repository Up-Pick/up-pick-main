package org.oneog.uppick.auction.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.oneog.uppick.auction")
public class OpenFeignConfig {

}
