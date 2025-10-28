package org.oneog.uppick.auction.domain.product.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.auction.domain.product.entity.Product;
import org.oneog.uppick.auction.domain.product.repository.ProductRepository;
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

		productViewCountIncreaseService.syncToDb();

		System.out.println("테스트 실행 시간: " + durationSeconds + "초");
		System.out.println("테스트 횟수: " + testcase);
		System.out.println("실패 횟수: " + failureCount.get());
		System.out.println("상품 조회수: " + productRepository.findById(productId).get().getViewCount());
	}

}
