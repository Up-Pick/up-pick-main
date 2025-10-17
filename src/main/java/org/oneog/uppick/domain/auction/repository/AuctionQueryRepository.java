package org.oneog.uppick.domain.auction.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Long findSellerIdByAuctionId(Long auctionId) {
		return jpaQueryFactory
			.select(product.registerId)
			.from(auction)
			.join(product).on(auction.productId.eq(product.id))
			.where(auction.id.eq(auctionId))
			.fetchOne();
	}
}
