package org.oneog.uppick.domain.ranking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "keyword", nullable = false)
	private String keyword;

	@Column(name = "rank", nullable = false)
	private Integer rank;

	public HotKeyword(String keyword, Integer rank) {
		this.keyword = keyword;
		this.rank = rank;
	}

}
