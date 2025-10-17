package org.oneog.uppick.common.config;

import org.oneog.uppick.common.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
// @PreAuthorize, @PostAuthorize, @Secured 모두 사용 가능
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	// 비밀번호 암호화용 Bean 등록
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 비활성화 (JWT 기반 인증이므로 필요 없음)
			.csrf(AbstractHttpConfigurer::disable)

			// 세션을 사용하지 않음 (JWT는 Stateless)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// 요청 경로별 접근 허용 설정
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // 컨트롤러에다가 @PreAuthorize("!isAuthenticated()") 이거 사용해야해~
			)

			// 불필요한 기본 로그인/로그아웃 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.rememberMe(AbstractHttpConfigurer::disable)

			// JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
