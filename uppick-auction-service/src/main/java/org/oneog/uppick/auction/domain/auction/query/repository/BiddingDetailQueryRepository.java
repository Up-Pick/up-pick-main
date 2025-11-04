package org.oneog.uppick.auction.domain.auction.query.repository;

import static org.oneog.uppick.auction.domain.auction.command.entity.QBiddingDetail.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BiddingDetailQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Long> findDistinctBidderIdsByAuctionExcludeMember(long auctionId, long excludeMemberId) {

		return jpaQueryFactory
			.select(biddingDetail.bidderId)
			.distinct()
			.from(biddingDetail)
			.where(biddingDetail.auctionId.eq(auctionId)
				.and(biddingDetail.bidderId.ne(excludeMemberId)))
			.fetch();
	}

}
