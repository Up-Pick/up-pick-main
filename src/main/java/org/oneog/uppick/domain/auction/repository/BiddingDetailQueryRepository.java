package org.oneog.uppick.domain.auction.repository;

import static org.oneog.uppick.domain.auction.entity.QBiddingDetail.*;

import java.util.List;
import java.util.Optional;

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

	// 현재 경매의 최고 입찰자 조회 (최근 입찰자 = 최고가 입찰자)
	public Optional<Long> findTopBidderIdByAuctionId(Long auctionId) {
		Long bidderId = jpaQueryFactory
			.select(biddingDetail.memberId)
			.from(biddingDetail)
			.where(biddingDetail.auctionId.eq(auctionId))
			.orderBy(biddingDetail.bidPrice.desc())
			.limit(1)
			.fetchOne();

		return Optional.ofNullable(bidderId);
	}
}
