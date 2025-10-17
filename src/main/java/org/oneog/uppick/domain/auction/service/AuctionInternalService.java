package org.oneog.uppick.domain.auction.service;

import java.util.List;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.notification.entity.NotificationType;
import org.oneog.uppick.domain.notification.service.NotificationExternalServiceApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionInternalService {

	// ***** Auction Domain ***** //
	private final AuctionRepository auctionRepository;
	private final AuctionQueryRepository auctionQueryRepository;
	private final AuctionMapper auctionMapper;

	private final BiddingDetailRepository biddingDetailRepository;

	// ****** External Domain API ***** //
	private final NotificationExternalServiceApi notificationExternalServiceApi;

	//특정 상품에 입찰 시도를 한다
	@Transactional
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {

		Auction auction = findAuctionById(auctionId);

		Long biddingPrice = request.getBiddingPrice(); //입찰 금액
		Long currentPrice = auction.getCurrentPrice(); //현재 입찰가
		Long minPrice = auction.getMinPrice(); //최소 입찰가

		// 1. 첫 입찰일 경우(currentPrice가 null) 최소 입찰가보다는 크거나 같아야한다.
		// 2. 현재 입찰가 보다 유저의 입찰금액이 더 커야함
		boolean validBid =
			(currentPrice == null && biddingPrice >= minPrice) ||
				(currentPrice != null && biddingPrice > currentPrice);

		if (validBid) {
			//성공 로직
			// 입찰 시 경매의 현재 입찰가가 갱신되어야한다.
			auction.updateCurrentPrice(request.getBiddingPrice());

			BiddingDetail biddingDetail = auctionMapper.toEntity(
				auctionId,
				memberId,
				request.getBiddingPrice()
			);
			// 입찰 시 입찰 내역에 기록이 남아야한다.
			biddingDetailRepository.save(biddingDetail);

			sendBidNotifications(auction, memberId, biddingPrice);
		} else {
			//입찰 실패 로직
			throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
		}

	}

	private Auction findAuctionById(long auctionId) {
		return auctionRepository.findById(auctionId)
			.orElseThrow(() -> new BusinessException(AuctionErrorCode.AUCTION_FOUND_FOUND));
	}

	/**
	 * 입찰 발생 시 알림 전송
	 */
	private void sendBidNotifications(Auction auction, Long bidderId, Long biddingPrice) {
		Long sellerId = auctionQueryRepository.findSellerIdByAuctionId(
			auction.getProductId()); //물품아이디를 통해 판매자 ID를 받아와야함

		// 판매자에게 알림
		notificationExternalServiceApi.sendNotification(
			sellerId,
			NotificationType.BID,
			"새로운 입찰이 도착했습니다!",
			"회원 " + bidderId + "님이 " + biddingPrice + "원으로 입찰했습니다."
		);

		// 해당 경매의 다른 입찰자들에게 알림
		List<Long> participantIds = biddingDetailRepository.findDistinctMemberIdsByAuctionId(auction.getId());

		for (Long participantId : participantIds) {
			if (!participantId.equals(bidderId)) { // 본인은 제외
				notificationExternalServiceApi.sendNotification(
					participantId,
					NotificationType.BID,
					"새로운 경쟁 입찰 발생",
					"다른 사용자가 " + biddingPrice + "원으로 입찰했습니다. 다시 입찰해보세요!"
				);
			}
		}

		// 입찰자 본인에게 확인 알림
		notificationExternalServiceApi.sendNotification(
			bidderId,
			NotificationType.TRADE,
			"입찰 완료",
			"상품 '" + auction.getProductId() + "'에 " + biddingPrice + "원으로 입찰이 완료되었습니다."
		);
	}
}
