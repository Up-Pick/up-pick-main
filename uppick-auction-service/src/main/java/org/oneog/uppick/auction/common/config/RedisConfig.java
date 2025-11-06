package org.oneog.uppick.auction.common.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// ObjectMapper 설정
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// GenericJackson2JsonRedisSerializer - List/Collection 타입용
		GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

		// Jackson2JsonRedisSerializer - 단일 객체용 (Projection 등)
		ObjectMapper objectMapperForSingle = new ObjectMapper();
		objectMapperForSingle.registerModule(new JavaTimeModule());
		objectMapperForSingle.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapperForSingle.activateDefaultTyping(
			objectMapperForSingle.getPolymorphicTypeValidator(),
			ObjectMapper.DefaultTyping.NON_FINAL,
			JsonTypeInfo.As.PROPERTY
		);
		Jackson2JsonRedisSerializer<Object> singleObjectSerializer = new Jackson2JsonRedisSerializer<>(
			objectMapperForSingle, Object.class);

		// 기본 캐시 설정: TTL 10분 (단일 객체용)
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.disableCachingNullValues()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(singleObjectSerializer));

		// 컬렉션용 캐시 설정
		RedisCacheConfiguration collectionConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.disableCachingNullValues()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer));

		// 캐시별 맞춤 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		// 카테고리 캐시: TTL 1시간, GenericSerializer 사용 (List 타입)
		cacheConfigurations.put("categories",
			collectionConfig.entryTtl(Duration.ofHours(1)));

		// 상품 상세 정보 캐시: TTL 5분, Jackson2JsonRedisSerializer 사용 (단일 객체)
		cacheConfigurations.put("productDetail",
			defaultConfig.entryTtl(Duration.ofMinutes(5)));

		// 상품 간단 정보 캐시: TTL 5분, Jackson2JsonRedisSerializer 사용 (단일 객체)
		cacheConfigurations.put("productSimpleInfo",
			defaultConfig.entryTtl(Duration.ofMinutes(5)));

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(defaultConfig)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}

}
