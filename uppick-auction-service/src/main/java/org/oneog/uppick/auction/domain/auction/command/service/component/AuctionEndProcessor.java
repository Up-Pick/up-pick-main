package org.oneog.uppick.auction.domain.auction.command.service.component;

import java.util.Optional;

import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
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

		// 이미 처리된 경매인지 확인 (멱등성 보장)
		if (auction.getStatus() != AuctionStatus.IN_PROGRESS) {
			log.debug("[Auction:{}] 이미 처리된 경매입니다. 현재 상태: {}", auctionId, auction.getStatus());
			return;
		}

		// 구매 확정 처리
		AuctionResult result = processAuctionResult(auction);

		// 경매 상태만 먼저 변경하고 커밋 (멱등성 보장)
		if (result.isSuccess()) {
			updateAuctionStatusAndCommit(auction, result);
		}
	}

	/**
	 * 경매 상태를 먼저 변경하고 커밋한 후, 후속 작업 처리
	 * 이렇게 하면 후속 작업 실패 시에도 재시도 방지 가능
	 */
	@Transactional
	protected void updateAuctionStatusAndCommit(Auction auction, AuctionResult result) {

		Long auctionId = auction.getId();

		if (result.isExpired()) {
			// 유찰 처리
			auction.markAsExpired();
			auctionRepository.save(auction);
			log.debug("[Auction:{}] 유찰 상태로 변경 완료", auctionId);

			// Redis 키 정리
			auctionRedisRepository.deleteAuctionKeys(auctionId);
			log.debug("[Auction:{}] Redis 키 정리 완료", auctionId);

		} else {
			// 낙찰 처리 - 경매 상태만 먼저 커밋
			auction.markAsSold();
			auctionRepository.save(auction);
			log.debug("[Auction:{}] 낙찰 상태로 변경 완료", auctionId);

			// 트랜잭션 커밋 후 후속 작업 처리 (별도 트랜잭션)
			processPostAuctionTasks(result);
		}
	}

	/**
	 * 경매 종료 후 후속 작업 처리 (별도 트랜잭션)
	 * - 구매/판매 내역 등록
	 * - 알림 전송
	 * - Elasticsearch 업데이트
	 * - Redis 정리
	 */
	@Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
	protected void processPostAuctionTasks(AuctionResult result) {

		Long auctionId = result.getAuctionId();
		Long buyerId = result.getBuyerId();
		Long sellerId = result.getSellerId();
		Long productId = result.getProductId();
		Long finalPrice = result.getFinalPrice();

		try {
			// 구매 내역 등록
			createPurchaseHistory(auctionId, buyerId, productId, finalPrice);

			// 판매 내역 등록
			createSellHistory(auctionId, sellerId, productId, finalPrice);

			// 알림 발송
			sendWinnerNotification(buyerId, productId, finalPrice);

			// Elasticsearch 업데이트
			productInnerService.updateProductDocumentStatus(productId);

			// Redis 키 정리
			auctionRedisRepository.deleteAuctionKeys(auctionId);
			log.debug("[Auction:{}] Redis 키 정리 완료", auctionId);

			log.debug("[Auction:{}] 후속 작업 완료", auctionId);

		} catch (Exception e) {
			log.error("[Auction:{}] 후속 작업 중 오류 발생 (경매 상태는 이미 커밋됨): {}", auctionId, e.getMessage(), e);
			// 경매 상태는 이미 FINISHED로 커밋되었으므로 재시도 시 멱등성 보장됨
			throw e;
		}
	}

	/**
	 * 경매 결과 판단 (낙찰 vs 유찰)
	 */
	private AuctionResult processAuctionResult(Auction auction) {

		Long auctionId = auction.getId();
		Long lastBidderId = auctionRedisRepository.findLastBidderId(auctionId);
		Long currentBidPrice = auctionRedisRepository.findCurrentBidPrice(auctionId);

		if (lastBidderId == null || currentBidPrice == null) {
			// 입찰자가 없을 경우: 유찰 처리
			log.debug("[Auction:{}] 유찰 처리 (입찰자 없음)", auctionId);
			return AuctionResult.expired(auctionId);
		}

		// 최고가 입찰 내역 1건 조회 (가격 기준 내림차순)
		Optional<BiddingDetail> topBid = biddingDetailRepository.findTopByAuctionIdAndBidderIdAndBidPrice(auctionId,
			lastBidderId, currentBidPrice);

		if (topBid.isEmpty()) {
			// 입찰자가 없을 경우: 유찰 처리
			log.debug("[Auction:{}] 유찰 처리 (입찰자 없음)", auctionId);
			return AuctionResult.expired(auctionId);
		}

		// 낙찰자 존재 시
		BiddingDetail winner = topBid.get();
		return AuctionResult.success(
			auctionId,
			winner.getBidderId(),
			auction.getRegisterId(),
			auction.getProductId(),
			winner.getBidPrice()
		);
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
	 * 경매 처리 결과를 담는 내부 클래스
	 */
	private static class AuctionResult {
		private final Long auctionId;
		private final Long buyerId;
		private final Long sellerId;
		private final Long productId;
		private final Long finalPrice;
		private final boolean success;
		private final boolean expired;

		private AuctionResult(Long auctionId, Long buyerId, Long sellerId, Long productId, Long finalPrice,
			boolean success, boolean expired) {
			this.auctionId = auctionId;
			this.buyerId = buyerId;
			this.sellerId = sellerId;
			this.productId = productId;
			this.finalPrice = finalPrice;
			this.success = success;
			this.expired = expired;
		}

		static AuctionResult success(Long auctionId, Long buyerId, Long sellerId, Long productId, Long finalPrice) {
			return new AuctionResult(auctionId, buyerId, sellerId, productId, finalPrice, true, false);
		}

		static AuctionResult expired(Long auctionId) {
			return new AuctionResult(auctionId, null, null, null, null, true, true);
		}

		boolean isSuccess() {
			return success;
		}

		boolean isExpired() {
			return expired;
		}

		Long getAuctionId() {
			return auctionId;
		}

		Long getBuyerId() {
			return buyerId;
		}

		Long getSellerId() {
			return sellerId;
		}

		Long getProductId() {
			return productId;
		}

		Long getFinalPrice() {
			return finalPrice;
		}
	}

}
