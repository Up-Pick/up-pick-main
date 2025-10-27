package org.oneog.uppick.domain.member.entity;

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
public class PurchaseDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "auction_id", nullable = false)
	private Long auctionId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "buyer_id", nullable = false)
	private Long buyerId;

	@Column(name = "purchase_price", nullable = false)
	private Long purchasePrice;

	@CreatedDate
	@Column(name = "purchase_at", nullable = false, updatable = false)
	private LocalDateTime purchaseAt;

	@Builder
	private PurchaseDetail(Long auctionId, Long productId, Long buyerId, Long purchasePrice) {

		this.auctionId = auctionId;
		this.productId = productId;
		this.buyerId = buyerId;
		this.purchasePrice = purchasePrice;
	}

}
