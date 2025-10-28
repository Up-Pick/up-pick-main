package org.oneog.uppick.auction.domain.auction.service;

import java.util.List;

import org.oneog.uppick.auction.common.lock.LockManager;
import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailQueryRepository;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.auction.domain.notification.enums.NotificationType;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

	private static final String BIDDING_LOCK_KEY_PREFIX = "auction:bidding:";

	// ***** Auction Domain ***** //
	private final BiddingDetailQueryRepository biddingDetailQueryRepository;
	private final BiddingProcessor biddingProcessor;

	// ****** Inner Service ***** //
	private final NotificationInnerService notificationInnerService;
	private final LockManager lockManager;

	// 특정 상품에 입찰 시도를 한다
	@Transactional(readOnly = false)
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {

		String lockKey = BIDDING_LOCK_KEY_PREFIX + auctionId;
		BiddingResultDto result = lockManager.executeWithLock(lockKey, () -> biddingProcessor.process(request,
			auctionId, memberId));

		sendBidNotifications(result.getSellerId(), auctionId, memberId, result.getBiddingPrice());
	}

	/**
	 * 입찰 발생 시 알림 전송
	 */
	private void sendBidNotifications(long sellerId, long auctionId, long bidderId, long biddingPrice) {

		// 판매자에게 알림
		SendNotificationRequest requestToSeller = SendNotificationRequest.builder()
			.memberId(sellerId)
			.type(NotificationType.BID)
			.string1("새로운 입찰이 도착했습니다!")
			.string2("회원 " + bidderId + "님이 " + biddingPrice + "원으로 입찰했습니다.")
			.build();

		notificationInnerService.sendNotification(requestToSeller);

		// 해당 경매의 다른 입찰자들에게 알림
		// JPA메서드로 가져오려다가 자꾸 타입 불일치 문제가 나와서 변경
		List<Long> participantIds = biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(auctionId);
		for (Long participantId : participantIds) {
			if (!participantId.equals(bidderId)) {

				// 본인은 제외
				SendNotificationRequest requestToParticipant = SendNotificationRequest.builder()
					.memberId(participantId)
					.type(NotificationType.BID)
					.string1("새로운 경쟁 입찰 발생")
					.string2("다른 사용자가 " + biddingPrice + "원으로 입찰했습니다. 다시 입찰해보세요!")
					.build();

				notificationInnerService.sendNotification(requestToParticipant);
			}
		}
	}

}
