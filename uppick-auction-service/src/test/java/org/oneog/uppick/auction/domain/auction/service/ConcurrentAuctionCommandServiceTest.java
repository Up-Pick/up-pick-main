package org.oneog.uppick.auction.domain.auction.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.command.model.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.auction.command.service.AuctionCommandService;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(RedissonAutoConfiguration.class)
@Transactional
@Disabled("동시성 테스트는 수동으로 실행하세요")
public class ConcurrentAuctionCommandServiceTest {

	@Autowired
	private AuctionCommandService auctionCommandService;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private BiddingDetailRepository biddingDetailRepository;

	@MockitoBean
	private MemberInnerService memberInnerService;

	@MockitoBean
	private NotificationInnerService notificationInnerService;

	private Long auctionId;
	private Long sellerId = 100L;
	private Long bidderId1 = 1L;
	private Long bidderId2 = 2L;

	@BeforeEach
	public void setUp() {

		// Mock MemberInnerService
		Mockito.when(memberInnerService.getMemberCredit(bidderId1)).thenReturn(2000L);
		Mockito.when(memberInnerService.getMemberCredit(bidderId2)).thenReturn(2000L);

		// Auction 생성
		Auction auction = Auction.builder()
			.productId(1L)
			.registerId(sellerId)
			.currentPrice(900L)
			.minPrice(500L)
			.status(AuctionStatus.IN_PROGRESS)
			.startAt(LocalDateTime.now().minusHours(1))
			.endAt(LocalDateTime.now().plusHours(1))
			.build();
		auction = auctionRepository.save(auction);
		auctionId = auction.getId();

		// 기존 입찰 생성
		BiddingDetail biddingDetail = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(3L)
			.bidPrice(900L)
			.build();
		biddingDetailRepository.save(biddingDetail);

		// 트랜잭션 커밋
		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	@Test
	public void testConcurrentBidding() throws Exception {

		AuctionBidRequest request1 = new AuctionBidRequest(1000L);
		AuctionBidRequest request2 = new AuctionBidRequest(1000L);

		int numberOfThreads = 2;
		CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		AtomicInteger successCount = new AtomicInteger(0);

		// 스레드 1: bidderId1으로 입찰
		executor.submit(() -> {
			try {
				barrier.await(); // 동시에 시작
				auctionCommandService.bid(request1, auctionId, bidderId1);
				successCount.incrementAndGet();
			} catch (Exception e) {
				System.out.println("Thread 1 exception: " + e.getMessage());
			} finally {
				latch.countDown();
			}
		});

		// 스레드 2: bidderId2으로 입찰
		executor.submit(() -> {
			try {
				barrier.await(); // 동시에 시작
				auctionCommandService.bid(request2, auctionId, bidderId2);
				successCount.incrementAndGet();
			} catch (Exception e) {
				System.out.println("Thread 2 exception: " + e.getMessage());
			} finally {
				latch.countDown();
			}
		});

		// 모든 작업 완료 대기
		latch.await();
		executor.shutdown();

		assertEquals(1, successCount.get());
	}

}
