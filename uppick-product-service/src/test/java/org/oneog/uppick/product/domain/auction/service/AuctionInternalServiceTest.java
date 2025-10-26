package org.oneog.uppick.product.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.product.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.product.domain.auction.entity.Auction;
import org.oneog.uppick.product.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.product.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.product.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.product.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.product.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.product.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.product.domain.auction.repository.BiddingDetailQueryRepository;
import org.oneog.uppick.product.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.product.domain.member.service.GetMemberCreditUseCase;
import org.oneog.uppick.product.domain.member.service.UpdateMemberCreditUseCase;
import org.oneog.uppick.product.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.product.domain.notification.service.SendNotificationUseCase;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuctionInternalServiceTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private AuctionQueryRepository auctionQueryRepository;
	@Mock
	private AuctionMapper auctionMapper;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;
	@Mock
	private BiddingDetailQueryRepository biddingDetailQueryRepository;
	@Mock
	private GetMemberCreditUseCase getMemberCreditUseCase;
	@Mock
	private UpdateMemberCreditUseCase updateMemberCreditUseCase;

	@Mock
	private SendNotificationUseCase sendNotificationUseCase;

	@InjectMocks
	private AuctionInternalService auctionInternalService;

	@Test
	void bid_입찰시도_성공() {
		// given
		long auctionId = 1L;
		long memberId = 100L; // 입찰자
		long sellerId = 200L; // 판매자
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(10L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(getMemberCreditUseCase.execute(anyLong())).willReturn(999999L);

		doNothing().when(updateMemberCreditUseCase).execute(anyLong(), anyLong());

		BiddingDetail biddingDetail = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(newBidPrice)
			.build();

		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
			.willReturn(biddingDetail);

		//알림전송
		doNothing().when(sendNotificationUseCase).execute(any(SendNotificationRequest.class));

		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
			.willReturn(biddingDetail);

		// when
		auctionInternalService.bid(request, auctionId, memberId);

		// then
		assertThat(auction.getCurrentPrice()).isEqualTo(newBidPrice);
		assertThat(auction.getLastBidderId()).isEqualTo(memberId);
		assertThat(auction.getCurrentPrice()).isEqualTo(newBidPrice);

		// 2. DB에 입찰 내역 호출
		verify(biddingDetailRepository).save(biddingDetail);

		// 크레딧이 차감 호출
		verify(updateMemberCreditUseCase).execute(memberId, -newBidPrice);

		// 5. 알림 로직 호출
		verify(biddingDetailQueryRepository).findDistinctBidderIdsByAuctionId(auctionId);

		// 6. 알림 호출
		verify(sendNotificationUseCase, times(1)).execute(any(SendNotificationRequest.class));
	}

	@Test
	void bid_잘못된입찰가_실패() {
		// given
		long auctionId = 1L;
		long memberId = 100L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(2000L)
			.registerId(10L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(1500L); // 현재입찰가(2000)보다 낮음

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(getMemberCreditUseCase.execute(anyLong())).willReturn(999999L);

		// when/then
		assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessage(AuctionErrorCode.WRONG_BIDDING_PRICE.getMessage());

	}

	@Test
	void bid_판매자_본인입찰_예외() {
		// given
		long auctionId = 1L;
		long memberId = 100L; // 입찰자 = 판매자
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(memberId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

		// when & then
		assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessage(AuctionErrorCode.CANNOT_BID_OWN_AUCTION.getMessage());

		verify(biddingDetailRepository, never()).save(any());
		verify(updateMemberCreditUseCase, never()).execute(anyLong(), anyLong());

	}
	//
	// @Test
	// void bid_크레딧부족_예외() {
	// 	// given
	// 	long auctionId = 1L;
	// 	long memberId = 100L;
	// 	long newBidPrice = 2000L;
	//
	// 	Auction auction = Auction.builder()
	// 		.productId(10L)
	// 		.minPrice(1000L)
	// 		.currentPrice(1500L)
	// 		.status(AuctionStatus.IN_PROGRESS)
	// 		.endAt(LocalDateTime.now().plusDays(1))
	// 		.build();
	//
	// 	AuctionBidRequest request = new AuctionBidRequest(newBidPrice);
	//
	// 	given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
	// 	given(auctionQueryRepository.findSellerIdByAuctionId(auctionId)).willReturn(200L);
	// 	given(auctionQueryRepository.findPointByMemberId(memberId)).willReturn(1000L);
	//
	// 	// when & then
	// 	assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
	// 		.isInstanceOf(BusinessException.class)
	// 		.hasMessageContaining("보유한 크레딧");
	//
	// 	verify(biddingDetailRepository, never()).save(any());
	// }
	//
	// @Test
	// void sendBidNotifications_입찰을한상황_알람내역데이터정상반환() {
	// 	// given
	// 	Auction auction = Auction.builder()
	// 		.productId(10L)
	// 		.minPrice(1000L)
	// 		.currentPrice(1500L)
	// 		.status(AuctionStatus.IN_PROGRESS)
	// 		.endAt(LocalDateTime.now().plusDays(1))
	// 		.build();
	//
	// 	//  Auction ID 세팅
	// 	ReflectionTestUtils.setField(auction, "id", 1L);
	//
	// 	long auctionId = 1L; // 테스트 중 사용할 경매 ID
	// 	long bidderId = 200L; // 입찰자(요청자)
	// 	long sellerId = 300L; // 판매자
	// 	long biddingPrice = 2500L; // 새로 입찰한 금액
	//
	// 	// 경매 ID로 경매를 찾으면 위에서 만든 auction 객체를 반환하도록 지정
	// 	given(auctionRepository.findById(anyLong())).willReturn(Optional.of(auction));
	// 	//경매 ID로 판매자 ID 조회 시 300L을 반환하도록 지정
	// 	given(auctionQueryRepository.findSellerIdByAuctionId(anyLong())).willReturn(sellerId);
	// 	//해당 경매에 참여한 사용자 목록 반환 (본인 포함)
	// 	given(biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(anyLong()))
	// 		.willReturn(List.of(100L, 200L, 150L));
	// 	given(auctionQueryRepository.findPointByMemberId(anyLong())).willReturn(999999L);
	// 	// when
	// 	auctionInternalService.bid(new AuctionBidRequest(biddingPrice), auctionId, bidderId);
	//
	// 	// then
	// 	//판매자한테 갈 알림
	// 	then(notificationExternalServiceApi).should()
	// 		.sendNotification(
	// 			eq(sellerId), eq(NotificationType.BID), anyString(), contains("입찰했습니다."));
	//
	// 	// 기존 참여자(본인 제외)에게 “경쟁 입찰” 알림이 2회 전송되었는지 확인
	// 	then(notificationExternalServiceApi).should(times(2))
	// 		.sendNotification(
	// 			anyLong(), eq(NotificationType.BID), eq("새로운 경쟁 입찰 발생"), anyString());
	// }

}
