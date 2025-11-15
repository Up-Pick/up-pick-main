package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.auction.command.service.component.AuctionEndProcessor;
import org.oneog.uppick.auction.domain.auction.query.repository.AuctionQueryRepository;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
import org.oneog.uppick.auction.domain.product.command.service.ProductInnerCommandService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuctionEndProcessorTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;
	@Mock
	private AuctionQueryRepository auctionQueryRepository;
	@Mock
	private AuctionRedisRepository auctionRedisRepository;

	@Mock
	private MemberInnerService memberInnerService;
	@Mock
	private NotificationInnerService notificationInnerService;
	@Mock
	private ProductInnerCommandService productInnerService;

	@InjectMocks
	private AuctionEndProcessor auctionSchedulerService;

	@Captor
	private ArgumentCaptor<RegisterPurchaseDetailRequest> purchaseCaptor;
	@Captor
	private ArgumentCaptor<RegisterSellDetailRequest> sellCaptor;
	@Captor
	private ArgumentCaptor<SendNotificationRequest> notificationCaptor;

	@Test
	void processAuctionWithBids_입찰자가존재하는경우_낙찰처리성공() {

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

		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(auction));
		given(auctionRedisRepository.findLastBidderId(auctionId))
			.willReturn(buyerId);
		given(auctionRedisRepository.findCurrentBidPrice(auctionId))
			.willReturn(bidPrice);
		given(biddingDetailRepository.findTopByAuctionIdAndBidderIdAndBidPrice(auctionId, buyerId, bidPrice))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findProductNameByProductId(productId))
			.willReturn(productName);

		willDoNothing().given(memberInnerService).registerPurchaseDetail(any(RegisterPurchaseDetailRequest.class));
		willDoNothing().given(memberInnerService).registerSellDetail(any(RegisterSellDetailRequest.class));
		willDoNothing().given(notificationInnerService).sendNotification(any(SendNotificationRequest.class));
		willDoNothing().given(productInnerService).updateProductDocumentStatus(anyLong());

		// when
		auctionSchedulerService.process(auctionId);

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
	void processAuctionWithoutBids_입찰자가없는경우_유찰처리() {

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
		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(auction));
		// Redis에 입찰 정보가 없도록 설정 (유찰)
		given(auctionRedisRepository.findLastBidderId(auctionId))
			.willReturn(null);
		given(auctionRedisRepository.findCurrentBidPrice(auctionId))
			.willReturn(null);

		// when
		auctionSchedulerService.process(auctionId);

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

	@Test
	void processAuctionCompletion_경매상태를알림발송전에변경_순서검증() {

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

		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(auction));
		given(auctionRedisRepository.findLastBidderId(auctionId))
			.willReturn(buyerId);
		given(auctionRedisRepository.findCurrentBidPrice(auctionId))
			.willReturn(bidPrice);
		given(biddingDetailRepository.findTopByAuctionIdAndBidderIdAndBidPrice(auctionId, buyerId, bidPrice))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findProductNameByProductId(productId))
			.willReturn(productName);

		willDoNothing().given(memberInnerService).registerPurchaseDetail(any(RegisterPurchaseDetailRequest.class));
		willDoNothing().given(memberInnerService).registerSellDetail(any(RegisterSellDetailRequest.class));
		willDoNothing().given(notificationInnerService).sendNotification(any(SendNotificationRequest.class));
		willDoNothing().given(productInnerService).updateProductDocumentStatus(anyLong());

		// when
		auctionSchedulerService.process(auctionId);

		// then - 호출 순서 검증: 경매 상태 변경이 알림 발송보다 먼저 일어나야 함
		var inOrder = inOrder(auctionRepository, memberInnerService, notificationInnerService);

		// 1. 경매 상태가 먼저 FINISHED로 변경되어 저장
		inOrder.verify(auctionRepository).save(argThat(a -> a.getStatus() == AuctionStatus.FINISHED));

		// 2. 그 다음 구매/판매 내역 등록
		inOrder.verify(memberInnerService).registerPurchaseDetail(any());
		inOrder.verify(memberInnerService).registerSellDetail(any());

		// 3. 마지막으로 알림 발송 (이미 경매 상태는 FINISHED)
		inOrder.verify(notificationInnerService).sendNotification(any());

	}

	@Test
	void process_이미처리된경매_재시도시중복처리방지() {

		// given
		Long auctionId = 1L;
		Long productId = 10L;
		Long sellerId = 200L;

		// 이미 낙찰 처리된 경매
		Auction finishedAuction = Auction.builder()
			.productId(productId)
			.registerId(sellerId)
			.status(AuctionStatus.FINISHED) // 이미 FINISHED 상태
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(finishedAuction, "id", auctionId);

		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(finishedAuction));

		// when
		auctionSchedulerService.process(auctionId);

		// then
		// 1. 이미 처리된 경매이므로 아무 작업도 수행되지 않음
		then(auctionRedisRepository).should(never()).findLastBidderId(anyLong());
		then(auctionRedisRepository).should(never()).findCurrentBidPrice(anyLong());
		then(biddingDetailRepository).should(never()).findTopByAuctionIdAndBidderIdAndBidPrice(anyLong(), anyLong(), anyLong());

		// 2. 구매/판매 내역 등록이 호출되지 않음
		then(memberInnerService).should(never()).registerPurchaseDetail(any());
		then(memberInnerService).should(never()).registerSellDetail(any());

		// 3. 알림 발송이 호출되지 않음 (무한 알림 방지!)
		then(notificationInnerService).should(never()).sendNotification(any());

		// 4. 경매 상태 재저장도 호출되지 않음
		then(auctionRepository).should(never()).save(any());
	}

	@Test
	void process_유찰처리된경매_재시도시중복처리방지() {

		// given
		Long auctionId = 1L;
		Long productId = 10L;
		Long sellerId = 200L;

		// 이미 유찰 처리된 경매
		Auction expiredAuction = Auction.builder()
			.productId(productId)
			.registerId(sellerId)
			.status(AuctionStatus.EXPIRED) // 이미 EXPIRED 상태
			.endAt(LocalDateTime.now().minusMinutes(1))
			.build();

		ReflectionTestUtils.setField(expiredAuction, "id", auctionId);

		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(expiredAuction));

		// when
		auctionSchedulerService.process(auctionId);

		// then
		// 1. 이미 처리된 경매이므로 아무 작업도 수행되지 않음
		then(auctionRedisRepository).should(never()).findLastBidderId(anyLong());
		then(auctionRedisRepository).should(never()).findCurrentBidPrice(anyLong());

		// 2. 경매 상태 재저장도 호출되지 않음
		then(auctionRepository).should(never()).save(any());
	}

	@Test
	void process_알림전송실패후재시도_멱등성보장() {

		// given - 첫 번째 시도 (알림 전송 실패 시나리오)
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

		// 첫 번째 호출 시: IN_PROGRESS 상태로 반환
		// 두 번째 호출 시: 이미 FINISHED 상태로 반환 (첫 번째 시도에서 상태가 변경되었다고 가정)
		given(auctionRepository.findById(auctionId))
			.willReturn(Optional.of(auction))  // 첫 번째: IN_PROGRESS
			.willReturn(Optional.of(auction)); // 두 번째: FINISHED (상태 변경됨)

		given(auctionRedisRepository.findLastBidderId(auctionId))
			.willReturn(buyerId);
		given(auctionRedisRepository.findCurrentBidPrice(auctionId))
			.willReturn(bidPrice);
		given(biddingDetailRepository.findTopByAuctionIdAndBidderIdAndBidPrice(auctionId, buyerId, bidPrice))
			.willReturn(Optional.of(topBid));
		given(auctionQueryRepository.findProductNameByProductId(productId))
			.willReturn(productName);

		willDoNothing().given(memberInnerService).registerPurchaseDetail(any(RegisterPurchaseDetailRequest.class));
		willDoNothing().given(memberInnerService).registerSellDetail(any(RegisterSellDetailRequest.class));

		// 첫 번째 알림 전송은 예외 발생 (실패 시나리오)
		willThrow(new RuntimeException("알림 서버 오류"))
			.given(notificationInnerService).sendNotification(any(SendNotificationRequest.class));

		// when - 첫 번째 시도: 알림 전송 실패로 예외 발생
		assertThatThrownBy(() -> auctionSchedulerService.process(auctionId))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("알림 서버 오류");

		// 경매 상태는 이미 FINISHED로 변경됨 (알림 전송 전에 커밋됨)
		assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FINISHED);

		// when - 두 번째 시도 (Kafka 재시도)
		auctionSchedulerService.process(auctionId);

		// then
		// 1. 두 번째 시도에서는 상태 체크로 인해 아무것도 실행 안됨
		then(memberInnerService).should(times(1)).registerPurchaseDetail(any()); // 첫 번째만
		then(memberInnerService).should(times(1)).registerSellDetail(any());      // 첫 번째만
		then(notificationInnerService).should(times(1)).sendNotification(any());  // 첫 번째만 (실패)

		// 2. 알림이 중복 전송되지 않음! (멱등성 보장)
		then(notificationInnerService).should(times(1)).sendNotification(any());
	}

}
