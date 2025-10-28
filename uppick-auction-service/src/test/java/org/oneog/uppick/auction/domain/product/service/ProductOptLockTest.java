package org.oneog.uppick.auction.domain.product.service;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.entity.Product;
import org.oneog.uppick.auction.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.auction.domain.product.repository.ProductRepository;
import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest
public class ProductOptLockTest {

	@Autowired
	private ProductInternalService productInternalService;
	@Autowired
	private ProductRepository productRepository;
	@MockitoBean
	private ProductQueryRepository productQueryRepository;
	@MockitoBean
	private MemberInnerService memberInnerService;

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
	@EnabledIf(expression = "#{systemProperties['skipTests'] != 'true'}", reason = "빌드 시 skip")
	void 낙관적_락_테스트() throws InterruptedException {

		AuthMember authMember = new AuthMember(1L, "닉네임");

		Product product = productRepository.findById(productId).get();

		ProductDetailProjection projection = new ProductDetailProjection(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getViewCount(),
			product.getRegisteredAt(),
			product.getImage(),
			"대충 카테고리",
			1_000L,
			1_500L,
			LocalDateTime.now().plusDays(1L),
			authMember.getMemberId()
		);

		given(productQueryRepository.getProductInfoById(productId)).willReturn(Optional.of(projection));
		given(memberInnerService.getMemberNickname(projection.getSellerId())).willReturn("판매자닉네임");

		int testcase = 1000;

		long startTime = System.currentTimeMillis();

		AtomicInteger failureCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(testcase);

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			for (int t = 0; t < testcase; t++) {
				executor.execute(() -> {
					try {
						productInternalService.getProductInfoById(productId, authMember);
					} catch (OptimisticLockingFailureException e) {
						failureCount.incrementAndGet();
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await();
		}

		long endTime = System.currentTimeMillis();
		double durationSeconds = (endTime - startTime) / 1000.0;

		System.out.println("테스트 실행 시간: " + durationSeconds + "초");
		System.out.println("테스트 횟수: " + testcase);
		System.out.println("실패 횟수: " + failureCount.get());
		System.out.println("상품 조회수: " + productRepository.findById(productId).get().getViewCount());
	}

}
