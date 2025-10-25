package org.oneog.uppick.product.domain.auction.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.oneog.uppick.product.domain.notification.enums.NotificationType;
import org.oneog.uppick.product.domain.notification.service.SendNotificationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSchedulerService {

	private final AuctionRepository auctionRepository;
	private final BiddingDetailRepository biddingDetailRepository;
	private final AuctionQueryRepository auctionQueryRepository;

	// ****** External Domain API ***** //
	private final RegisterPurchaseDetailUseCase registerPurchaseDetailUseCase;
	private final RegisterSellDetailUseCase registerSellDetailUseCase;
	private final SendNotificationUseCase sendNotificationUseCase;

	@Transactional
	@Scheduled(cron = "0 0 * * * *") // 매 시 정각마다 실행
	public void confirmFinishedAuctions() {
		LocalDateTime now = LocalDateTime.now();

		// 종료 시간이 현재 시간 이전인 '진행 중' 경매 조회
		List<Auction> endedAuctions = auctionRepository.findAllByEndAtBeforeAndStatus(now,
			AuctionStatus.IN_PROGRESS);

		// 확인용
		log.info("[Scheduler] 종료된 경매 {}건 처리 시작...", endedAuctions.size());

		// 각 경매별 구매 확정 처리
		for (Auction auction : endedAuctions) {
			processAuctionResult(auction);
		}

		log.info("[Scheduler] 경매 마감 처리 완료.");
	}

	private void processAuctionResult(Auction auction) {
		Long auctionId = auction.getId();

		// 최고가 입찰 내역 1건 조회 (가격 기준 내림차순)
		Optional<BiddingDetail> topBid = biddingDetailRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId);

		if (topBid.isEmpty()) {
			// 입찰자가 없을 경우: 유찰 처리
			handleExpiredAuction(auction);
			log.info("[Auction:{}] 유찰 처리 (입찰자 없음)", auctionId);
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
			log.info("[Auction:{}] 유찰 상태로 변경 완료", auctionId);

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
		Long buyerId = winner.getMemberId(); //상품을 구매한사람
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
	}

	/**
	 *  구매 내역 등록
	 */
	private void createPurchaseHistory(Long auctionId, Long buyerId, Long productId, Long price) {
		RegisterPurchaseDetailRequest request = new RegisterPurchaseDetailRequest(auctionId, buyerId, productId, price);
		registerPurchaseDetailUseCase.execute(request);
		log.info("구매내역 등록: 구매자={}, 상품={}, 금액={}", buyerId, productId, price);
	}

	/**
	 *  판매 내역 등록
	 */
	private void createSellHistory(Long auctionId, Long sellerId, Long productId, Long price) {
		RegisterSellDetailRequest request = new RegisterSellDetailRequest(auctionId, sellerId, productId, price);
		registerSellDetailUseCase.execute(request);
		log.info("판매내역 등록: 판매자={}, 상품={}, 금액={}", sellerId, productId, price);
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

		sendNotificationUseCase.execute(request);

		log.info("[Notification] 낙찰자 {}에게 알림 전송 완료", memberId);
	}

	/**
	 *  경매 상태 변경 후 DB 반영
	 */
	private void updateAuctionStatus(Auction auction) {
		auction.markAsSold();
		auctionRepository.save(auction);
		log.info("경매 {} 상태 저장 완료", auction.getId());
	}
}
