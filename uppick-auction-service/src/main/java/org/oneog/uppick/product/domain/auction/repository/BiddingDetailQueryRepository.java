package org.oneog.uppick.product.domain.auction.repository;

import static org.oneog.uppick.product.domain.auction.entity.QBiddingDetail.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BiddingDetailQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Long> findDistinctBidderIdsByAuctionId(Long auctionId) {

		return jpaQueryFactory
			.select(biddingDetail.memberId).distinct()
			.from(biddingDetail)
			.where(biddingDetail.auctionId.eq(auctionId))
			.fetch();
	}

}
