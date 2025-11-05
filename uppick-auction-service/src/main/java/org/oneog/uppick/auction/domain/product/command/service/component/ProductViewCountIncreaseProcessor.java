package org.oneog.uppick.auction.domain.product.command.service.component;

import org.oneog.uppick.auction.domain.product.command.repository.ProductRedisRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductViewCountIncreaseProcessor {

	private static final String VIEW_KEY = "product:view:%d";
	private final ProductRedisRepository productRedisRepository;

	// 조회수 증가 (Redis에만 저장)
	// DB 동기화는 batch-service의 ViewCountBatchScheduler에서 매분마다 처리
	public void process(Long productId) {

		String key = String.format(VIEW_KEY, productId);
		productRedisRepository.increment(key);
	}

}
