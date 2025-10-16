package org.oneog.uppick.domain.auction.entity;

import java.time.LocalDateTime;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.auction.exception.AuctionErrorCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "current_price", nullable = true)
	private Long currentPrice; //현재 입찰가

	@Column(name = "min_price", nullable = false)
	private Long minPrice; //최소 입찰가

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	@CreatedDate
	@Column(name = "start_at", nullable = false, updatable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Builder
	private Auction(Long productId, Long currentPrice, Long minPrice, Status status, LocalDateTime endAt) {
		this.productId = productId;
		this.currentPrice = currentPrice;
		this.minPrice = minPrice;
		this.status = status;
		this.endAt = endAt;
	}

	// --- 도메인 메서드 ---
	//입찰 성공시 현재 입찰가를 갱신하기 위함
	public void updateCurrentPrice(Long biddingPrice) {
		if (biddingPrice == null || biddingPrice <= 0) {
			throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
		}
		this.currentPrice = biddingPrice;
	}
}
