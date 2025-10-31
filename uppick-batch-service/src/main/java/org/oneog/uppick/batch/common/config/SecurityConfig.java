package org.oneog.uppick.batch.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

	// Prometheus 메트릭 수집을 위한 Actuator 엔드포인트 허용
	@Bean
	public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {

		http
			.securityMatcher("/actuator/**")
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authz -> authz
				.anyRequest().permitAll());

		return http.build();
	}

}

