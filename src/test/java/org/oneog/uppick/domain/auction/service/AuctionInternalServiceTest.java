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
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailQueryRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.notification.entity.NotificationType;
import org.oneog.uppick.domain.notification.service.NotificationExternalServiceApi;
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
	private NotificationExternalServiceApi notificationExternalServiceApi;

	@InjectMocks
	private AuctionInternalService auctionInternalService;

	@Test
	void bid_입찰시도_성공() {
		// given
		long auctionId = 1L;
		long memberId = 100L;   // 입찰자
		long sellerId = 200L;   // 판매자
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(auctionQueryRepository.findSellerIdByAuctionId(auctionId)).willReturn(sellerId);

		BiddingDetail biddingDetail = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(newBidPrice)
			.build();

		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
			.willReturn(biddingDetail);

		// when
		auctionInternalService.bid(request, auctionId, memberId);

		// then
		assertThat(auction.getCurrentPrice()).isEqualTo(newBidPrice);
		verify(biddingDetailRepository).save(biddingDetail);
		verify(auctionQueryRepository).findSellerIdByAuctionId(auctionId);
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
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(1500L); // 현재입찰가(2000)보다 낮음

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

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
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(auctionQueryRepository.findSellerIdByAuctionId(auctionId)).willReturn(memberId); // 판매자 == 입찰자

		// when & then
		assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("본인의 판매 물품에는 입찰할 수 없습니다.");

		verify(biddingDetailRepository, never()).save(any());
		verify(auctionQueryRepository).findSellerIdByAuctionId(auctionId);
	}

	@Test
	void bid_크레딧부족_예외() {
		// given
		long auctionId = 1L;
		long memberId = 100L;
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(auctionQueryRepository.findSellerIdByAuctionId(auctionId)).willReturn(200L);
		given(auctionQueryRepository.findPointByMemberId(memberId)).willReturn(1000L);

		// when & then
		assertThatThrownBy(() -> auctionInternalService.bid(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("보유한 크레딧");

		verify(biddingDetailRepository, never()).save(any());
	}

	@Test
	void sendBidNotifications_입찰을한상황_알람내역데이터정상반환() {
		// given
		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		//  Auction ID 세팅
		ReflectionTestUtils.setField(auction, "id", 1L);

		long auctionId = 1L; // 테스트 중 사용할 경매 ID
		long bidderId = 200L;  // 입찰자(요청자)
		long sellerId = 300L; // 판매자
		long biddingPrice = 2500L; // 새로 입찰한 금액

		// 경매 ID로 경매를 찾으면 위에서 만든 auction 객체를 반환하도록 지정
		given(auctionRepository.findById(anyLong())).willReturn(Optional.of(auction));
		//경매 ID로 판매자 ID 조회 시 300L을 반환하도록 지정
		given(auctionQueryRepository.findSellerIdByAuctionId(anyLong())).willReturn(sellerId);
		//해당 경매에 참여한 사용자 목록 반환 (본인 포함)
		given(biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(anyLong()))
			.willReturn(List.of(100L, 200L, 150L));

		// when
		auctionInternalService.bid(new AuctionBidRequest(biddingPrice), auctionId, bidderId);

		// then
		//판매자한테 갈 알림
		then(notificationExternalServiceApi).should().sendNotification(
			eq(sellerId), eq(NotificationType.BID), anyString(), contains("입찰했습니다.")
		);

		// 기존 참여자(본인 제외)에게 “경쟁 입찰” 알림이 2회 전송되었는지 확인
		then(notificationExternalServiceApi).should(times(2)).sendNotification(
			anyLong(), eq(NotificationType.BID), eq("새로운 경쟁 입찰 발생"), anyString()
		);
	}

}
