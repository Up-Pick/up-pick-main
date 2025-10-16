package org.oneog.uppick.domain.bid.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BidQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;
}
