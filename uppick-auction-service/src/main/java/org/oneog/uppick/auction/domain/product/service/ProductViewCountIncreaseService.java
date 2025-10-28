package org.oneog.uppick.auction.domain.product.service;

import java.util.Objects;
import java.util.Set;

import org.oneog.uppick.auction.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductViewCountIncreaseService {

	private static final String VIEW_KEY = "product:view:%d";
	private final StringRedisTemplate redisTemplate;
	private final ProductRepository productRepository;

	// 조회수 증가
	public void increaseProductViewCount(Long productId) {

		String key = String.format(VIEW_KEY, productId);
		redisTemplate.opsForValue().increment(key);
	}

	// 1분마다 동기화 처리
	@Scheduled(fixedRate = 60_000)
	@Transactional
	public void syncToDb() {

		Set<String> keys = redisTemplate.keys("product:view:*");
		for (String key : keys) {
			Long productId = Long.parseLong(key.split(":")[2]);
			long increase = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));

			if (increase > 0) {
				productRepository.incrementViewCount(productId, increase);
				redisTemplate.delete(key);
			}
		}
	}

}
