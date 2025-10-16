package org.oneog.uppick.domain.ranking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class HotDeal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ranking;

	@Column(nullable = false)
	private String productName;

	@Column(nullable = false)
	private String productImage;

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Builder
	private HotDeal(Long ranking, String productName, String productImage) {
		this.ranking = ranking;
		this.productName = productName;
		this.productImage = productImage;
	}

}
