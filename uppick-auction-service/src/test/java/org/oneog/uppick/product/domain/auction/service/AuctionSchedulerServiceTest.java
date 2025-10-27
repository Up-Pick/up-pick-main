package org.oneog.uppick.product.domain.auction.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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
import org.oneog.uppick.product.domain.auction.entity.Auction;
import org.oneog.uppick.product.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.product.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.product.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.product.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.product.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.product.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.product.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.product.domain.member.service.RegisterPurchaseDetailUseCase;
import org.oneog.uppick.product.domain.member.service.RegisterSellDetailUseCase;
import org.oneog.uppick.product.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.product.domain.notification.service.SendNotificationUseCase;
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
	private RegisterPurchaseDetailUseCase registerPurchaseDetailUseCase;
	@Mock
	private RegisterSellDetailUseCase registerSellDetailUseCase;

	@Mock
	private SendNotificationUseCase sendNotificationUseCase;

	@InjectMocks
	private AuctionSchedulerService auctionSchedulerService;

	@Captor
	private ArgumentCaptor<RegisterPurchaseDetailRequest> purchaseCaptor;
	@Captor
	private ArgumentCaptor<RegisterSellDetailRequest> sellCaptor;
	@Captor
	private ArgumentCaptor<SendNotificationRequest> notificationCaptor;

	@Test
	void confirmFinishedAuctions_입찰자가존재하는경우_success() {
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

		given(auctionRepository.findAllByEndAtBeforeAndStatus(any(), eq(AuctionStatus.IN_PROGRESS)))
			.willReturn(List.of(auction));
		given(biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findProductNameByProductId(productId)).willReturn(productName);

		doNothing().when(registerPurchaseDetailUseCase)
			.execute(any(RegisterPurchaseDetailRequest.class));
		doNothing().when(registerSellDetailUseCase).execute(any(RegisterSellDetailRequest.class));
		doNothing().when(sendNotificationUseCase).execute(any(SendNotificationRequest.class));

		// when
		auctionSchedulerService.confirmFinishedAuctions();

		// then 정상적으로 호출되었는지 확인
		assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FINISHED);

		//구매/판매 내역 등록 캡처
		then(registerPurchaseDetailUseCase).should().execute(purchaseCaptor.capture());
		then(registerSellDetailUseCase).should().execute(sellCaptor.capture());

		then(sendNotificationUseCase).should(times(1))
			.execute(
				notificationCaptor.capture());

		// 경매 상태 저장 (판매 완료)
		then(auctionRepository).should().save(auction);

		// 구매 내역 DTO 검증
		RegisterPurchaseDetailRequest purchaseRequest = purchaseCaptor.getValue();

		assertThat(purchaseRequest.getBuyerId()).isEqualTo(buyerId);
		assertThat(purchaseRequest.getProductId()).isEqualTo(productId);
		assertThat(purchaseRequest.getPrice()).isEqualTo(bidPrice);

		// 판매 내역 DTO 검증
		RegisterSellDetailRequest sellRequest = sellCaptor.getValue();
		assertThat(sellRequest.getSellerId()).isEqualTo(sellerId);
		assertThat(sellRequest.getPrice()).isEqualTo(bidPrice);

		// 알림 DTO 검증
		SendNotificationRequest notification = notificationCaptor.getValue();
		assertThat(notification.getMemberId()).isEqualTo(buyerId);
		assertThat(notification.getString1()).contains("낙찰");
		assertThat(notification.getString2()).contains(productName);
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
