package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.auction.entity.Auction;
import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.auction.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
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
	private MemberInnerService memberInnerService;
	@Mock
	private NotificationInnerService notificationInnerService;

	@InjectMocks
	private AuctionSchedulerService auctionSchedulerService;

	@Captor
	private ArgumentCaptor<RegisterPurchaseDetailRequest> purchaseCaptor;
	@Captor
	private ArgumentCaptor<RegisterSellDetailRequest> sellCaptor;
	@Captor
	private ArgumentCaptor<SendNotificationRequest> notificationCaptor;
	@Captor
	private ArgumentCaptor<Auction> auctionCaptor;

	@Test
	void confirmFinishedAuctions_입찰자가존재하는경우_낙찰처리성공() {
		// given
		Long auctionId = 1L;
		Long buyerId = 100L;
		Long sellerId = 200L;
		Long productId = 10L;
		Long bidPrice = 3000L;
		String productName = "아이폰 15";

		Auction auction = Auction.builder()
			.productId(productId)
			.registerId(sellerId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		BiddingDetail topBid = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(buyerId)
			.bidPrice(bidPrice)
			.build();

		given(auctionRepository.findAllByEndAtBeforeAndStatus(any(LocalDateTime.class), eq(AuctionStatus.IN_PROGRESS)))
			.willReturn(List.of(auction));
		given(biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findProductNameByProductId(productId))
			.willReturn(productName);

		willDoNothing().given(memberInnerService).registerPurchaseDetail(any(RegisterPurchaseDetailRequest.class));
		willDoNothing().given(memberInnerService).registerSellDetail(any(RegisterSellDetailRequest.class));
		willDoNothing().given(notificationInnerService).sendNotification(any(SendNotificationRequest.class));

		// when
		auctionSchedulerService.confirmFinishedAuctions();

		// then
		// 1. 경매 상태 변경 확인
		assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FINISHED);

		// 2. 구매 내역 등록 검증
		then(memberInnerService).should().registerPurchaseDetail(purchaseCaptor.capture());
		RegisterPurchaseDetailRequest purchaseRequest = purchaseCaptor.getValue();
		assertThat(purchaseRequest.getAuctionId()).isEqualTo(auctionId);
		assertThat(purchaseRequest.getBuyerId()).isEqualTo(buyerId);
		assertThat(purchaseRequest.getProductId()).isEqualTo(productId);
		assertThat(purchaseRequest.getPrice()).isEqualTo(bidPrice);

		// 3. 판매 내역 등록 검증
		then(memberInnerService).should().registerSellDetail(sellCaptor.capture());
		RegisterSellDetailRequest sellRequest = sellCaptor.getValue();
		assertThat(sellRequest.getAuctionId()).isEqualTo(auctionId);
		assertThat(sellRequest.getSellerId()).isEqualTo(sellerId);
		assertThat(sellRequest.getProductId()).isEqualTo(productId);
		assertThat(sellRequest.getPrice()).isEqualTo(bidPrice);

		// 4. 알림 발송 검증
		then(notificationInnerService).should().sendNotification(notificationCaptor.capture());
		SendNotificationRequest notification = notificationCaptor.getValue();
		assertThat(notification.getMemberId()).isEqualTo(buyerId);
		assertThat(notification.getString1()).contains("낙찰");
		assertThat(notification.getString2()).contains(productName, bidPrice.toString());

		// 5. 경매 저장 검증
		then(auctionRepository).should().save(auction);
	}

	@Test
	void confirmFinishedAuctions_입찰자가없는경우_유찰처리() {
		// given
		Long auctionId = 1L;
		Long productId = 10L;
		Long sellerId = 200L;

		Auction auction = Auction.builder()
			.productId(productId)
			.registerId(sellerId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		// 경매는 종료되었지만 입찰자가 없음
		given(auctionRepository.findAllByEndAtBeforeAndStatus(any(LocalDateTime.class), eq(AuctionStatus.IN_PROGRESS)))
			.willReturn(List.of(auction));
		given(biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId))
			.willReturn(Optional.empty());

		// when
		auctionSchedulerService.confirmFinishedAuctions();

		// then
		// 1. 경매 상태가 유찰로 변경되었는지 확인
		assertThat(auction.getStatus()).isEqualTo(AuctionStatus.EXPIRED);

		// 2. 경매 저장 검증
		then(auctionRepository).should().save(auction);

		// 3. 구매/판매 내역 등록이 호출되지 않았는지 확인
		then(memberInnerService).should(never()).registerPurchaseDetail(any());
		then(memberInnerService).should(never()).registerSellDetail(any());

		// 4. 알림 발송이 호출되지 않았는지 확인
		then(notificationInnerService).should(never()).sendNotification(any());
	}

}
