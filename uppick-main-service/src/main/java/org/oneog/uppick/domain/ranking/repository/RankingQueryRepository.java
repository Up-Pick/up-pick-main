package org.oneog.uppick.domain.ranking.repository;

import static org.oneog.uppick.domain.searching.entity.QSearchHistory.*;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.ranking.dto.HotKeywordCalculationDto;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingQueryRepository {

	private final JPAQueryFactory queryFactory;

	//주간 인기검색어 상위 10개의 키워드 조회
	public List<HotKeywordCalculationDto> findTop10HotKeywordsByCount() {

		LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

		return queryFactory
			.select(Projections.constructor(
				HotKeywordCalculationDto.class,
				searchHistory.keyword
			))
			.from(searchHistory)
			.where(searchHistory.searchedAt.goe(sevenDaysAgo))
			.groupBy(searchHistory.keyword)
			.orderBy(searchHistory.count().desc())
			.limit(10)
			.fetch();
	}

}