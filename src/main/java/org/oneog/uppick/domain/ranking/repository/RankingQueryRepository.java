package org.oneog.uppick.domain.ranking.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.auction.entity.QBiddingDetail.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;
import static org.oneog.uppick.domain.searching.entity.QSearchHistory.*;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.ranking.dto.HotDealCalculationDto;
import org.oneog.uppick.domain.ranking.dto.HotKeywordCalculationDto;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingQueryRepository {

	private final JPAQueryFactory queryFactory;

	//입찰수가 많은 상위 6개의 상품 조회
	public List<HotDealCalculationDto> findTop6HotDealsByBidCount() {
		LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

		return queryFactory
			.select(Projections.constructor(
				HotDealCalculationDto.class,
				product.id,
				product.name,
				product.image
			))
			.from(product)
			.leftJoin(auction).on(auction.productId.eq(product.id))
			.leftJoin(biddingDetail).on(biddingDetail.auctionId.eq(auction.id))
			.where(biddingDetail.bidAt.goe(oneDayAgo))
			.groupBy(product.id, product.name, product.image)
			.orderBy(biddingDetail.count().desc())
			.limit(6)
			.fetch();
	}

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
