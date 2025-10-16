package org.oneog.uppick.domain.category.entity;

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
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String big;

	@Column(nullable = false)
	private String small;

	@Column(nullable = false, updatable = false)
	private LocalDateTime created_at = LocalDateTime.now();

	@Builder
	private Category(String big, String small) {
		this.big = big;
		this.small = small;
	}
}
