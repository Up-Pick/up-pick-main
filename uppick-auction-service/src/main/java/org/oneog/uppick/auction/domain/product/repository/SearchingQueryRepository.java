package org.oneog.uppick.auction.domain.product.repository;

import java.util.List;

import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.entity.QAuction;
import org.oneog.uppick.auction.domain.product.dto.projection.SearchProductProjection;
import org.oneog.uppick.auction.domain.product.entity.QProduct;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchingQueryRepository {

	private static final QAuction AUCTION = QAuction.auction;
	private static final QProduct PRODUCT = QProduct.product;
	;
	private final JPAQueryFactory queryFactory;

	public List<SearchProductProjection> findProductWithIds(List<Long> productIds) {

		return queryFactory
			.select(Projections.constructor(SearchProductProjection.class,
				PRODUCT.id,
				PRODUCT.image,
				PRODUCT.name,
				PRODUCT.registeredAt,
				AUCTION.endAt,
				AUCTION.currentPrice,
				AUCTION.minPrice,
				AUCTION.status.eq(AuctionStatus.FINISHED)))
			.from(PRODUCT)
			.join(AUCTION)
			.where(PRODUCT.id.in(productIds))
			.fetch();
	}

}
