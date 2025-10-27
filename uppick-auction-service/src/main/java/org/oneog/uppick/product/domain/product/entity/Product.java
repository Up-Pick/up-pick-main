package org.oneog.uppick.product.domain.product.entity;

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
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "description", nullable = false)
	private String description;

	private Long viewCount = 0L;

	@CreatedDate
	@Column(name = "registered_at", nullable = false, updatable = false)
	private LocalDateTime registeredAt;

	@Column(name = "image")
	private String image;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;
	@Column(name = "register_id", nullable = false)
	private Long registerId;

	@Column(name = "big_category", nullable = false)
	private String bigCategory;
	@Column(name = "small_category", nullable = false)
	private String smallCategory;

	@Builder
	private Product(String name, String description, String image, Long categoryId, Long registerId, String bigCategory,
		String smallCategory) {
		this.name = name;
		this.description = description;
		this.image = image;
		this.categoryId = categoryId;
		this.registerId = registerId;
		this.bigCategory = bigCategory;
		this.smallCategory = smallCategory;
	}

	// ***** Method ***** //
	public void increaseViewCount() {
		this.viewCount++;
	}
}