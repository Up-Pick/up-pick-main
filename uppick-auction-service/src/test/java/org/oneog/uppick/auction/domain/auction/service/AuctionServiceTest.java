package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.common.lock.LockManager;
import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.entity.Auction;
import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.auction.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailQueryRepository;
import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.auction.domain.notification.enums.NotificationType;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private AuctionMapper auctionMapper;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;
	@Mock
	private BiddingDetailQueryRepository biddingDetailQueryRepository;
	@Mock
	private MemberInnerService memberInnerService;
	@Mock
	private NotificationInnerService notificationInnerService;
	@Mock
	private LockManager lockManager;

	@InjectMocks
	private AuctionService auctionService;

	@Captor
	private ArgumentCaptor<SendNotificationRequest> notificationCaptor;

	@Test
	void sendBidNotifications_입찰을한상황_알람내역데이터정상반환() {

		// given
		long auctionId = 1L;
		long bidderId = 200L; // 입찰자(요청자)
		long sellerId = 300L; // 판매자
		long biddingPrice = 2500L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(sellerId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		given(biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(auctionId))
			.willReturn(List.of(100L, 200L, 150L));

		BiddingResultDto biddingResult = new BiddingResultDto(sellerId, biddingPrice);
		given(lockManager.executeWithLock(anyString(), ArgumentMatchers.<Supplier<BiddingResultDto>>any())).willReturn(
			biddingResult);

		// when
		auctionService.bid(new AuctionBidRequest(biddingPrice), auctionId, bidderId);

		// then
		then(notificationInnerService).should(times(3)).sendNotification(notificationCaptor.capture());

		// 캡처된 3개의 요청을 리스트로 가져옴
		List<SendNotificationRequest> capturedRequests = notificationCaptor.getAllValues();

		// 판매자 알림 검증
		SendNotificationRequest sellerNotification = capturedRequests.stream()
			.filter(req -> req.getMemberId().equals(sellerId))
			.findFirst()
			.orElseThrow(() -> new AssertionError("판매자 알림이 없습니다."));

		assertThat(sellerNotification.getType()).isEqualTo(NotificationType.BID);
		assertThat(sellerNotification.getString1()).isEqualTo("새로운 입찰이 도착했습니다!");
		assertThat(sellerNotification.getString2()).contains(bidderId + "님", biddingPrice + "원");

		List<SendNotificationRequest> participantNotifications = capturedRequests.stream()
			.filter(req -> !req.getMemberId().equals(sellerId))
			.toList();

		assertThat(participantNotifications).hasSize(2); // 본인(200L) 제외 2명

		assertThat(participantNotifications).extracting(SendNotificationRequest::getMemberId)
			.containsExactlyInAnyOrder(100L, 150L);

		assertThat(participantNotifications).allMatch(req -> req.getType().equals(NotificationType.BID) &&
			req.getString1().equals("새로운 경쟁 입찰 발생") &&
			req.getString2().contains(biddingPrice + "원")
		);
	}

}
