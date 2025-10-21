package org.oneog.uppick.domain.product.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductViewHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_id", nullable = false)
	private Long productId;
	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@CreatedDate
	@Column(name = "viewed_at", nullable = false, updatable = false)
	private LocalDateTime viewedAt;

	public ProductViewHistory(Long productId, Long memberId) {
		this.productId = productId;
		this.memberId = memberId;
	}
}
