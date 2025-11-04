package org.oneog.uppick.auction.domain.product.query.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.auction.domain.product.command.entity.Product;
import org.oneog.uppick.auction.domain.product.command.repository.ProductRepository;
import org.oneog.uppick.auction.domain.product.command.service.ProductViewCountIncreaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
public class ProductViewCountConcurrencyTest {

	@Autowired
	private ProductViewCountIncreaseService productViewCountIncreaseService;
	@Autowired
	private ProductRepository productRepository;

	private Long productId;

	@BeforeEach
	public void setUp() {

		Product product = productRepository.saveAndFlush(
			Product.builder()
				.name("상품")
				.description("설명")
				.image("이미지경로")
				.categoryId(1L)
				.registerId(1L)
				.bigCategory("대분류")
				.smallCategory("소분류")
				.build());
		productId = product.getId();
	}

	@Test
	void redis_동시성_테스트() throws InterruptedException {

		int testcase = 5000;

		long startTime = System.currentTimeMillis();

		AtomicInteger failureCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(testcase);

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			for (int t = 0; t < testcase; t++) {
				executor.execute(() -> {
					try {
						productViewCountIncreaseService.increaseProductViewCount(productId);
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await();
		}

		long endTime = System.currentTimeMillis();
		double durationSeconds = (endTime - startTime) / 1000.0;

		// DB 동기화는 batch-service에서 자동으로 처리됨
		// productViewCountIncreaseService.syncToDb(); // 제거됨

		System.out.println("테스트 실행 시간: " + durationSeconds + "초");
		System.out.println("테스트 횟수: " + testcase);
		System.out.println("실패 횟수: " + failureCount.get());
		// 조회수는 Redis에만 저장되므로 DB에서 조회할 수 없음
		// System.out.println("상품 조회수: " + productRepository.findById(productId).get().getViewCount());
	}

}
