package org.oneog.uppick.product.domain.auction.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BiddingDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "auction_id", nullable = false)
	private Long auctionId;

	@Column(name = "bidder_id", nullable = false)
	private Long memberId;

	@Column(name = "bid_price", nullable = false)
	private Long bidPrice;

	@CreatedDate
	@Column(name = "bid_at", nullable = false, updatable = false)
	private LocalDateTime bidAt; //입찰시각

	@Builder
	private BiddingDetail(Long auctionId, Long memberId, Long bidPrice) {

		this.auctionId = auctionId;
		this.memberId = memberId;
		this.bidPrice = bidPrice;
	}

}

