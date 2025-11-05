package org.oneog.uppick.batch.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 설정
 *
 * 조회수 배치 처리용
 * - ViewCountItemReader: Redis에서 product:view:* 패턴의 키 조회
 * - ViewCountItemWriter: 처리 완료 후 Redis 키 삭제
 */
@Slf4j
@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {

		log.debug("Redis Connection Factory 초기화 - host: {}, port: {}", host, port);

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(host);
		redisConfig.setPort(port);

		return new LettuceConnectionFactory(redisConfig);
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate() {

		log.debug("StringRedisTemplate 초기화");

		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory());

		return template;
	}

}
