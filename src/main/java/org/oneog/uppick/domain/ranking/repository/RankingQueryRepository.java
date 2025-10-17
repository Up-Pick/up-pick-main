package org.oneog.uppick.domain.ranking.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.auction.entity.QBiddingDetail.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.ranking.dto.HotDealCalculationDto;
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

}
