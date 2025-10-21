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
import org.oneog.uppick.domain.auction.repository.BiddingDetailQueryRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.notification.entity.NotificationType;
import org.oneog.uppick.domain.notification.service.NotificationExternalServiceApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionInternalService {

	// ***** Auction Domain ***** //
	private final AuctionRepository auctionRepository;
	private final AuctionQueryRepository auctionQueryRepository;
	private final AuctionMapper auctionMapper;

	private final BiddingDetailRepository biddingDetailRepository;
	private final BiddingDetailQueryRepository biddingDetailQueryRepository;
	// ****** External Domain API ***** //
	private final NotificationExternalServiceApi notificationExternalServiceApi;

	//특정 상품에 입찰 시도를 한다
	@Transactional
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {
		try {
			Auction auction = findAuctionById(auctionId);

			// 판매자 본인 입찰 방지
			if (auctionQueryRepository.findSellerIdByAuctionId(auctionId).equals(memberId)) {
				throw new BusinessException(AuctionErrorCode.CANNOT_BID_OWN_AUCTION);
			}

			Long biddingPrice = request.getBiddingPrice();
			Long currentPrice = auction.getCurrentPrice();
			Long minPrice = auction.getMinPrice();
			//  포인트 잔액 확인
			Long memberPoint = auctionQueryRepository.findPointByMemberId(memberId);
			if (biddingPrice > memberPoint) {
				throw new BusinessException(AuctionErrorCode.INSUFFICIENT_CREDIT);
			}
			boolean validBid =
				(currentPrice == null && biddingPrice >= minPrice) ||
					(currentPrice != null && biddingPrice > currentPrice);

			if (validBid) {
				auction.updateCurrentPrice(request.getBiddingPrice());

				BiddingDetail biddingDetail = auctionMapper.toEntity(
					auctionId,
					memberId,
					request.getBiddingPrice()
				);
				biddingDetailRepository.save(biddingDetail);

				log.info("알림 전송 시작");
				sendBidNotifications(auction, memberId, biddingPrice);
				log.info("알림 전송 완료");

			} else {
				throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
			}
		} catch (Exception e) {
			log.error("입찰 처리 중 예외 발생", e);
			throw e;
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
		//JPA메서드로 가져오려다가 자꾸 타입 불일치 문제가 나와서 변경
		List<Long> participantIds = biddingDetailQueryRepository.findDistinctBidderIdsByAuctionId(auction.getId());
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
	}
}
