package org.oneog.uppick.domain.member.command.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SellDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "final_price", nullable = false)
	private Long finalPrice;

	@CreatedDate
	@Column(name = "sell_at", nullable = false, updatable = false)
	private LocalDateTime sellAt;

	@Column(name = "auction_id", nullable = false)
	private Long auctionId;
	@Column(name = "product_id", nullable = false)
	private Long productId;
	@Column(name = "seller_id", nullable = false)
	private Long sellerId;

	@Builder
	private SellDetail(Long finalPrice, Long auctionId, Long productId, Long sellerId) {

		this.finalPrice = finalPrice;
		this.auctionId = auctionId;
		this.productId = productId;
		this.sellerId = sellerId;
	}

}
