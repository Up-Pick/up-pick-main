package org.oneog.uppick.domain.ranking.entity;

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
public class HotDeal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "rank_no", nullable = false)
	private Integer rankNo;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "product_image", nullable = false)
	private String productImage;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Builder
	private HotDeal(Long productId, Integer rankNo, String productName, String productImage) {
		this.productId = productId;
		this.rankNo = rankNo;
		this.productName = productName;
		this.productImage = productImage;
	}

}
