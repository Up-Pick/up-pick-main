package org.oneog.uppick.auction.domain.auction.service;

// package org.oneog.uppick.auction.domain.auction.service;
//
// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.BDDMockito.*;
//
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;
//
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Captor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.oneog.uppick.common.exception.BusinessException;
// import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
// import org.oneog.uppick.auction.domain.auction.entity.Auction;
// import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
// import org.oneog.uppick.auction.domain.auction.entity.BiddingDetail;
// import org.oneog.uppick.auction.domain.auction.exception.AuctionErrorCode;
// import org.oneog.uppick.auction.domain.auction.mapper.AuctionMapper;
// import org.oneog.uppick.auction.domain.auction.repository.AuctionRepository;
// import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailQueryRepository;
// import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailRepository;
// import org.oneog.uppick.auction.domain.member.service.GetMemberCreditUseCase;
// import org.oneog.uppick.auction.domain.member.service.UpdateMemberCreditUseCase;
// import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
// import org.oneog.uppick.auction.domain.notification.enums.NotificationType;
// import org.oneog.uppick.auction.domain.notification.service.SendNotificationUseCase;
// import org.springframework.test.util.ReflectionTestUtils;
//
// @ExtendWith(MockitoExtension.class)
// public class AuctionServiceTest {
//
// 	@Mock
// 	private AuctionRepository auctionRepository;
//
// 	@Mock
// 	private AuctionMapper auctionMapper;
// 	@Mock
// 	private BiddingDetailRepository biddingDetailRepository;
// 	@Mock
// 	private BiddingDetailQueryRepository biddingDetailQueryRepository;
// 	@Mock
// 	private GetMemberCreditUseCase getMemberCreditUseCase;
// 	@Mock
// 	private UpdateMemberCreditUseCase updateMemberCreditUseCase;
//
// 	@Mock
// 	private SendNotificationUseCase sendNotificationUseCase;
//
// 	@InjectMocks
// 	private AuctionService auctionService;
//
// 	@Captor
// 	private ArgumentCaptor<SendNotificationRequest> notificationCaptor;
//
// 	@Test
// 	void bid_입찰시도_성공() {
// 		// given
// 		long auctionId = 1L;
// 		long memberId = 100L; // 입찰자
// 		long sellerId = 200L; // 판매자
// 		long newBidPrice = 2000L;
//
// 		Auction auction = Auction.builder()
// 			.productId(10L)
// 			.minPrice(1000L)
// 			.currentPrice(1500L)
// 			.registerId(sellerId)
// 			.status(AuctionStatus.IN_PROGRESS)
// 			.endAt(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		ReflectionTestUtils.setField(auction, "id", auctionId);
//
// 		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);
//
// 		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
// 		given(getMemberCreditUseCase.execute(anyLong())).willReturn(999999L);
//
// 		doNothing().when(updateMemberCreditUseCase).execute(anyLong(), anyLong());
//
// 		BiddingDetail biddingDetail = BiddingDetail.builder()
// 			.auctionId(auctionId)
// 			.memberId(memberId)
// 			.bidPrice(newBidPrice)
// 			.build();
//
// 		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
// 			.willReturn(biddingDetail);
//
// 		//알림전송
// 		doNothing().when(sendNotificationUseCase).execute(any(SendNotificationRequest.class));
//
// 		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
// 			.willReturn(biddingDetail);
//
// 		// when
// 		auctionService.bid(request, auctionId, memberId);
//
// 		// then
// 		assertThat(auction.getCurrentPrice()).isEqualTo(newBidPrice);
// 		assertThat(auction.getLastBidderId()).isEqualTo(memberId);
//
// 		// 2. DB에 입찰 내역 호출
// 		verify(biddingDetailRepository).save(biddingDetail);
//
// 		// 크레딧이 차감 호출
// 		verify(updateMemberCreditUseCase).execute(memberId, -newBidPrice);
//
// 		// 5. 알림 로직 호출
// 		verify(biddingDetailQueryRepository).findDistinctBidderIdsByAuctionId(auctionId);
//
// 		// 6. 알림 호출
// 		verify(sendNotificationUseCase, times(1)).execute(any(SendNotificationRequest.class));
// 	}
//
// 	@Test
// 	void bid_잘못된입찰가_실패() {
// 		// given
// 		long auctionId = 1L;
// 		long memberId = 100L;
//
// 		Auction auction = Auction.builder()
// 			.productId(10L)
// 			.minPrice(1000L)
// 			.currentPrice(2000L)
// 			.registerId(10L)
// 			.status(AuctionStatus.IN_PROGRESS)
// 			.endAt(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		AuctionBidRequest request = new AuctionBidRequest(1500L); // 현재입찰가(2000)보다 낮음
//
// 		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
// 		given(getMemberCreditUseCase.execute(anyLong())).willReturn(999999L);
//
// 		// when/then
// 		assertThatThrownBy(() -> auctionService.bid(request, auctionId, memberId))
// 			.isInstanceOf(BusinessException.class)
// 			.hasMessage(AuctionErrorCode.WRONG_BIDDING_PRICE.getMessage());
//
// 	}
//
// 	@Test
// 	void bid_판매자_본인입찰_예외() {
// 		// given
// 		long auctionId = 1L;
// 		long memberId = 100L; // 입찰자 = 판매자
// 		long newBidPrice = 2000L;
//
// 		Auction auction = Auction.builder()
// 			.productId(10L)
// 			.minPrice(1000L)
// 			.currentPrice(1500L)
// 			.registerId(memberId)
// 			.status(AuctionStatus.IN_PROGRESS)
// 			.endAt(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);
//
// 		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
//
// 		// when & then
// 		assertThatThrownBy(() -> auctionService.bid(request, auctionId, memberId))
// 			.isInstanceOf(BusinessException.class)
// 			.hasMessage(AuctionErrorCode.CANNOT_BID_OWN_AUCTION.getMessage());
//
// 		verify(biddingDetailRepository, never()).save(any());
// 		verify(updateMemberCreditUseCase, never()).execute(anyLong(), anyLong());
//
// 	}
//
// 	@Test
// 	void bid_크레딧부족_예외() {
// 		// given
// 		long auctionId = 1L;
// 		long memberId = 100L;
// 		long newBidPrice = 2000L;
//
// 		Auction auction = Auction.builder()
// 			.productId(10L)
// 			.minPrice(1000L)
// 			.currentPrice(1500L)
// 			.registerId(10L)
// 			.status(AuctionStatus.IN_PROGRESS)
// 			.endAt(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);
//
// 		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
// 		given(getMemberCreditUseCase.execute(anyLong())).willReturn(1000L);
//
// 		// when & then
// 		assertThatThrownBy(() -> auctionService.bid(request, auctionId, memberId))
// 			.isInstanceOf(BusinessException.class)
// 			.hasMessageContaining(AuctionErrorCode.INSUFFICIENT_CREDIT.getMessage());
//
// 		verify(biddingDetailRepository, never()).save(any());
// 	}
//
// 	@Test
// 	void sendBidNotifications_입찰을한상황_알람내역데이터정상반환() {
// 		// given
// 		long auctionId = 1L;
// 		long bidderId = 200L; // 입찰자(요청자)
// 		long sellerId = 300L; // 판매자
// 		long biddingPrice = 2500L;
//
// 		Auction auction = Auction.builder()
// 			.productId(10L)
// 			.minPrice(1000L)
// 			.currentPrice(1500L)
// 			.registerId(sellerId)
// 			.status(AuctionStatus.IN_PROGRESS)
// 			.endAt(LocalDateTime.now().plusDays(1))
// 			.build();
//
// 		ReflectionTestUtils.setField(auction, "id", auctionId);
//
// 		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
// 		given(getMemberCreditUseCase.execute(bidderId)).willReturn(999999L);
// 		doNothing().when(updateMemberCreditUseCase).execute(anyLong(), anyLong());
// 		given(auctionMapper.toEntity(anyLong(), anyLong(), anyLong()))
// 			.willReturn(BiddingDetail.builder().build());
//
// 		given(biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(auctionId))
// 			.willReturn(List.of(100L, 200L, 150L));
//
// 		// when
// 		auctionService.bid(new AuctionBidRequest(biddingPrice), auctionId, bidderId);
//
// 		// then
// 		then(sendNotificationUseCase).should(times(3)).execute(notificationCaptor.capture());
//
// 		// 캡처된 3개의 요청을 리스트로 가져옴
// 		List<SendNotificationRequest> capturedRequests = notificationCaptor.getAllValues();
//
// 		//판매자 알림 검증
// 		SendNotificationRequest sellerNotification = capturedRequests.stream()
// 			.filter(req -> req.getMemberId().equals(sellerId))
// 			.findFirst()
// 			.orElseThrow(() -> new AssertionError("판매자 알림이 없습니다."));
//
// 		assertThat(sellerNotification.getType()).isEqualTo(NotificationType.BID);
// 		assertThat(sellerNotification.getString1()).isEqualTo("새로운 입찰이 도착했습니다!");
// 		assertThat(sellerNotification.getString2()).contains(bidderId + "님", biddingPrice + "원");
//
// 		List<SendNotificationRequest> participantNotifications = capturedRequests.stream()
// 			.filter(req -> !req.getMemberId().equals(sellerId))
// 			.toList();
//
// 		assertThat(participantNotifications).hasSize(2); // ⭐️ 본인(200L) 제외 2명
//
// 		assertThat(participantNotifications).extracting(SendNotificationRequest::getMemberId)
// 			.containsExactlyInAnyOrder(100L, 150L);
//
// 		assertThat(participantNotifications).allMatch(req ->
// 			req.getType().equals(NotificationType.BID) &&
// 				req.getString1().equals("새로운 경쟁 입찰 발생") &&
// 				req.getString2().contains(biddingPrice + "원")
// 		);
// 	}
//
// }
