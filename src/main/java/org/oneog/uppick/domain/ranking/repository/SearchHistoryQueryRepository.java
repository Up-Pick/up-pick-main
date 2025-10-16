package org.oneog.uppick.domain.ranking.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchHistoryQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;
	
}
