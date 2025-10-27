package org.oneog.uppick.auction.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.auction.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.product.dto.projection.SearchProductProjection;
import org.oneog.uppick.auction.domain.auction.entity.QAuction;
import org.oneog.uppick.auction.domain.product.entity.QProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchingQueryRepository {

	private static final QAuction AUCTION = QAuction.auction;
	private static final QProduct PRODUCT = QProduct.product;
	private static final String SORT_END_AT_DESC = "endAtDesc";
	private final JPAQueryFactory jpaQueryFactory;

	public Page<SearchProductProjection> findProductsWithFilters(
		Pageable pageable,
		long categoryId,
		LocalDateTime endAtFrom,
		boolean onlyNotSold,
		String sortBy,
		String keyword) {

		BooleanExpression whereClause = buildWhereClause(categoryId, endAtFrom, onlyNotSold, keyword);

		List<SearchProductProjection> results = buildSelectQuery(whereClause, sortBy, pageable).fetch();

		JPAQuery<Long> countQuery = buildCountQuery(whereClause);

		return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
	}

	private OrderSpecifier<?> getOrderSpecifier(String sortBy, QAuction auction,
		QProduct product) {

		if (SORT_END_AT_DESC.equals(sortBy)) {

			return auction.endAt.desc();
		} else {

			// 기본: 등록 날짜 내림차순
			return product.registeredAt.desc();
		}
	}

	private BooleanExpression buildWhereClause(long categoryId, LocalDateTime endAtFrom, boolean onlyNotSold,
		String keyword) {

		BooleanExpression clause = categoryPredicate(categoryId);

		if (endAtFrom != null) {

			clause = clause.and(endAtPredicate(endAtFrom));
		}

		if (onlyNotSold) {

			clause = clause.and(onlyNotSoldPredicate());
		}

		if (keyword != null && !keyword.isBlank()) {

			clause = clause.and(nameContains(keyword));
		}

		return clause;
	}

	private BooleanExpression categoryPredicate(long categoryId) {

		return PRODUCT.categoryId.eq(categoryId);
	}

	private BooleanExpression endAtPredicate(LocalDateTime endAtFrom) {

		return AUCTION.endAt.goe(endAtFrom);
	}

	private BooleanExpression onlyNotSoldPredicate() {

		return AUCTION.status.ne(AuctionStatus.FINISHED);
	}

	private BooleanExpression nameContains(String keyword) {

		return PRODUCT.name.contains(keyword);
	}

	private JPAQuery<SearchProductProjection> buildSelectQuery(BooleanExpression whereClause, String sortBy,
		Pageable pageable) {

		return jpaQueryFactory
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
			.on(PRODUCT.id.eq(AUCTION.productId))
			.where(whereClause)
			.orderBy(
				new CaseBuilder()
					.when(AUCTION.status.eq(AuctionStatus.FINISHED))
					.then(1)
					.otherwise(0)
					.asc(), // 0(미완료) 먼저, 1(완료) 나중
				getOrderSpecifier(sortBy, AUCTION, PRODUCT))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());
	}

	private JPAQuery<Long> buildCountQuery(BooleanExpression whereClause) {

		return jpaQueryFactory
			.select(PRODUCT.count())
			.from(PRODUCT)
			.join(AUCTION)
			.on(PRODUCT.id.eq(AUCTION.productId))
			.where(whereClause);
	}

}
