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

	@Column(name = "rank_no", nullable = false)
	private Integer rankNo;

	public HotKeyword(String keyword, Integer rankNo) {

		this.keyword = keyword;
		this.rankNo = rankNo;
	}

}