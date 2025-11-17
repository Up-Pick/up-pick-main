package org.oneog.uppick.auction.domain.auction.command.service.component;

import java.util.Optional;

import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.auction.common.exception.AuctionErrorCode;
import org.oneog.uppick.auction.domain.auction.query.repository.AuctionQueryRepository;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.auction.domain.notification.dto.request.SendNotificationRequest;
import org.oneog.uppick.auction.domain.notification.enums.NotificationType;
import org.oneog.uppick.auction.domain.notification.service.NotificationInnerService;
import org.oneog.uppick.auction.domain.product.command.service.ProductInnerCommandService;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionEndProcessor {

	private final AuctionRepository auctionRepository;
	private final BiddingDetailRepository biddingDetailRepository;
	private final AuctionQueryRepository auctionQueryRepository;

	// ****** Inner Service API ***** //
	private final MemberInnerService memberInnerService;
	private final NotificationInnerService notificationInnerService;
	private final ProductInnerCommandService productInnerService;

	private final AuctionRedisRepository auctionRedisRepository;

	@Transactional
	public void process(long auctionId) {

		// 종료 시간이 현재 시간 이전인 '진행 중' 경매 조회
		Auction auction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new BusinessException(AuctionErrorCode.AUCTION_NOT_FOUND));

		// 구매 확정 처리
		processAuctionResult(auction);
	}

	private void processAuctionResult(Auction auction) {

		Long auctionId = auction.getId();
		Long lastBidderId = auctionRedisRepository.findLastBidderId(auctionId);
		Long currentBidPrice = auctionRedisRepository.findCurrentBidPrice(auctionId);

		if (lastBidderId == null || currentBidPrice == null) {
			// 입찰자가 없을 경우: 유찰 처리
			handleExpiredAuction(auction);
			log.debug("[Auction:{}] 유찰 처리 (입찰자 없음)", auctionId);
			return;
		}

		// 최고가 입찰 내역 1건 조회 (가격 기준 내림차순)
		Optional<BiddingDetail> topBid = biddingDetailRepository.findTopByAuctionIdAndBidderIdAndBidPrice(auctionId,
			lastBidderId, currentBidPrice);

		if (topBid.isEmpty()) {
			// 입찰자가 없을 경우: 유찰 처리
			handleExpiredAuction(auction);
			log.debug("[Auction:{}] 유찰 처리 (입찰자 없음)", auctionId);
			return;
		}

		// 낙찰자 존재 시
		BiddingDetail winner = topBid.get();
		handleAuctionCompletion(auction, winner);

	}

	private void handleExpiredAuction(Auction auction) {

		Long auctionId = auction.getId();

		try {
			auction.markAsExpired();
			// 경매 상태변경
			auctionRepository.save(auction);
			log.debug("[Auction:{}] 유찰 상태로 변경 완료", auctionId);

			// Redis 키 정리 (입찰가, 입찰자)
			auctionRedisRepository.deleteAuctionKeys(auctionId);
			log.debug("[Auction:{}] Redis 키 정리 완료", auctionId);

		} catch (Exception e) {
			log.error("[Auction:{}] 유찰 처리 중 오류 발생: {}", auctionId, e.getMessage());
			throw e;
		}
	}

	/**
	 *  경매 낙찰 확정 처리
	 * - 알림 발송
	 * - 구매내역 등록
	 * - 판매내역 등록
	 * - 상품 판매시각 갱신
	 * - 경매 상태 업데이트
	 */
	private void handleAuctionCompletion(Auction auction, BiddingDetail winner) {

		Long buyerId = winner.getBidderId(); //상품을 구매한사람
		Long productId = auction.getProductId();
		Long finalPrice = winner.getBidPrice();
		Long auctionId = auction.getId();
		Long sellerId = auction.getRegisterId(); // 상품을 판매한사람

		//  구매 내역 등록
		createPurchaseHistory(auctionId, buyerId, productId, finalPrice);

		// 판매 내역 등록
		createSellHistory(auctionId, sellerId, productId, finalPrice);

		//  알림 발송
		sendWinnerNotification(buyerId, productId, finalPrice);

		// 경매 상태 업데이트 (DB 반영)
		updateAuctionStatus(auction);

		// 경매 상태 업데이트 (Elasticsearch 반영)
		productInnerService.updateProductDocumentStatus(productId);

		// Redis 키 정리 (입찰가, 입찰자)
		auctionRedisRepository.deleteAuctionKeys(auctionId);
		log.debug("[Auction:{}] Redis 키 정리 완료", auctionId);
	}

	/**
	 *  구매 내역 등록
	 */
	private void createPurchaseHistory(Long auctionId, Long buyerId, Long productId, Long price) {

		RegisterPurchaseDetailRequest request = new RegisterPurchaseDetailRequest(auctionId, buyerId, productId, price);
		memberInnerService.registerPurchaseDetail(request);
		log.debug("구매내역 등록: 구매자={}, 상품={}, 금액={}", buyerId, productId, price);
	}

	/**
	 *  판매 내역 등록
	 */
	private void createSellHistory(Long auctionId, Long sellerId, Long productId, Long price) {

		RegisterSellDetailRequest request = new RegisterSellDetailRequest(auctionId, sellerId, productId, price);
		memberInnerService.registerSellDetail(request);
		log.debug("판매내역 등록: 판매자={}, 상품={}, 금액={}", sellerId, productId, price);
	}

	/**
	 *  낙찰자 알림 발송
	 */
	private void sendWinnerNotification(Long memberId, Long productId, Long price) {

		String productName = auctionQueryRepository.findProductNameByProductId(productId);

		SendNotificationRequest request = SendNotificationRequest.builder()
			.memberId(memberId)
			.type(NotificationType.TRADE)
			.string1("낙찰을 축하드립니다!")
			.string2("상품 '" + productName + "'을 " + price + "원에 낙찰받았습니다.")
			.build();

		notificationInnerService.sendNotification(request);

		log.debug("[Notification] 낙찰자 {}에게 알림 전송 완료", memberId);
	}

	/**
	 *  경매 상태 변경 후 DB 반영
	 */
	private void updateAuctionStatus(Auction auction) {

		auction.markAsSold();
		auctionRepository.save(auction);
		log.debug("경매 {} 상태 저장 완료", auction.getId());
	}

}
