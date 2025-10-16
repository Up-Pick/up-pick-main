package org.oneog.uppick.domain.category.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;
}
