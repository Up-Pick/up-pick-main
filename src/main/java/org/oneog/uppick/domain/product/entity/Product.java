package org.oneog.uppick.domain.product.entity;

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
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "description", nullable = false)
	private String description;

	private Long viewCount = 0L;

	@Column(name = "registered_at", nullable = false)
	private LocalDateTime registeredAt = LocalDateTime.now();
	@Column(name = "sold_out_at")
	private LocalDateTime soldOutAt;

	@Column(name = "image")
	private String image;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;
	@Column(name = "register_id", nullable = false)
	private Long registerId;

	@Builder
	private Product(String name, String description, String image, Long categoryId, Long registerId) {
		this.name = name;
		this.description = description;
		this.image = image;
		this.categoryId = categoryId;
		this.registerId = registerId;
	}
}