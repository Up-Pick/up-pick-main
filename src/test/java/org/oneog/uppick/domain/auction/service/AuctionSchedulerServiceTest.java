package org.oneog.uppick.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.member.service.MemberExternalServiceApi;
import org.oneog.uppick.domain.notification.service.NotificationExternalServiceApi;
import org.oneog.uppick.domain.product.service.ProductExternalServiceApi;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerServiceTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;
	@Mock
	private AuctionQueryRepository auctionQueryRepository;
	@Mock
	private MemberExternalServiceApi memberExternalServiceApi;
	@Mock
	private ProductExternalServiceApi productExternalServiceApi;
	@Mock
	private NotificationExternalServiceApi notificationExternalServiceApi;

	@InjectMocks
	private AuctionSchedulerService auctionSchedulerService;

	@Test
	void confirmFinishedAuctions_입찰자가존재하는경우_success() {
		// given
		Long auctionId = 1L;
		Long buyerId = 100L;
		Long sellerId = 200L;
		Long productId = 10L;
		Long bidPrice = 3000L;

		Auction auction = Auction.builder()
			.productId(productId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		BiddingDetail topBid = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(buyerId)
			.bidPrice(bidPrice)
			.build();

		// mock 동작 정의
		given(auctionRepository.findAllByEndAtBeforeAndStatus(any(), eq(AuctionStatus.IN_PROGRESS)))
			.willReturn(List.of(auction));
		given(biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findSellerIdByAuctionId(auctionId)).willReturn(sellerId);
		given(auctionQueryRepository.findProductNameByProductId(productId)).willReturn("아이폰 15");

		// when
		auctionSchedulerService.confirmFinishedAuctions();

		// then 정상적으로 호출되었는지 확인
		//  구매/판매 내역 등록
		then(memberExternalServiceApi).should().registerPurchaseDetail(auctionId, buyerId, productId, bidPrice);
		then(memberExternalServiceApi).should().registerSellDetail(auctionId, sellerId, productId, bidPrice);

		//  알림 발송
		then(notificationExternalServiceApi).should().sendNotification(
			eq(buyerId),
			any(),
			contains("낙찰"),
			contains("아이폰 15")
		);

		//  경매 상태 저장 (판매 완료)
		then(auctionRepository).should().save(auction);
	}

	@Test
	void confirmFinishedAuctions_유찰되는경우_유찰상태로변경() {
		// given
		Long auctionId = 1L;
		Long productId = 10L;

		Auction auction = Auction.builder()
			.productId(productId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		// 경매는 종료되었지만 입찰자가 없음
		given(auctionRepository.findAllByEndAtBeforeAndStatus(any(), eq(AuctionStatus.IN_PROGRESS)))
			.willReturn(List.of(auction));
		given(biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId))
			.willReturn(Optional.empty());

		// when
		auctionSchedulerService.confirmFinishedAuctions();

		// then
		//  경매가 저장되었는지 (markAsExpired → save 호출됐는지)
		then(auctionRepository).should().save(auction);

		// 상태가 실제로 바뀌었는지
		assertThat(auction.getStatus()).isEqualTo(AuctionStatus.EXPIRED); //
	}

}
