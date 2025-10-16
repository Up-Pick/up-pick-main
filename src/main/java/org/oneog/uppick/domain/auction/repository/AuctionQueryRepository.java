package org.oneog.uppick.domain.auction.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;
}
