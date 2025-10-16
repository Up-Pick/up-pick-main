package org.oneog.uppick.domain.bid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class Bid {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = true)
	private Long currentPrice; //현재 입찰가

	@Column(nullable = false)
	private Long minPrice; //최소 입찰가

	@Column(nullable = false)
	private Enum status;

	@Column(nullable = false, updatable = false)
	private LocalDateTime startAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime endAt;

	@Builder
	private Bid(Long productId, Long currentPrice, Long minPrice, Enum status, LocalDateTime endAt) {
		this.productId = productId;
		this.currentPrice = currentPrice;
		this.minPrice = minPrice;
		this.status = status;
		this.endAt = endAt;
	}
}
