package org.oneog.uppick.auction.domain.product.command.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductViewCountIncreaseService {

	private static final String VIEW_KEY = "product:view:%d";
	private final StringRedisTemplate redisTemplate;

	// 조회수 증가 (Redis에만 저장)
	// DB 동기화는 batch-service의 ViewCountBatchScheduler에서 매분마다 처리
	public void increaseProductViewCount(Long productId) {

		String key = String.format(VIEW_KEY, productId);
		redisTemplate.opsForValue().increment(key);
	}

}
