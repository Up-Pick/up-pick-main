package org.oneog.uppick.domain.searching.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.oneog.uppick.domain.auction.entity.QAuction;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.product.entity.QProduct;
import org.oneog.uppick.domain.searching.dto.response.SearchProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchingQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    // QueryDSL Q-types for reuse
    private static final QAuction AUCTION = QAuction.auction;
    private static final QProduct PRODUCT = QProduct.product;

    // Defaults
    private static final long DEFAULT_CATEGORY_ID = 1L;
    private static final String SORT_END_AT_DESC = "endAtDesc";

    public Page<SearchProductProjection> findProductsWithFilters(
        Pageable pageable,
        Long categoryId,
        LocalDateTime endAtFrom,
        Boolean onlyNotSold,
        String sortBy) {
        BooleanExpression whereClause = buildWhereClause(categoryId, endAtFrom, onlyNotSold);

        List<SearchProductProjection> results = buildSelectQuery(whereClause, sortBy, pageable).fetch();

        JPAQuery<Long> countQuery = buildCountQuery(whereClause);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    private com.querydsl.core.types.OrderSpecifier<?> getOrderSpecifier(String sortBy, QAuction auction,
        QProduct product) {
        if (SORT_END_AT_DESC.equals(sortBy)) {
            return auction.endAt.desc();
        }

        // 기본: 등록 날짜 내림차순
        return product.registeredAt.desc();
    }

    /**
     * Build the combined where-clause from provided optional filters.
     */
    private BooleanExpression buildWhereClause(Long categoryId, LocalDateTime endAtFrom, Boolean onlyNotSold) {
        BooleanExpression clause = categoryPredicate(categoryId);

        if (endAtFrom != null) {
            clause = clause.and(endAtPredicate(endAtFrom));
        }

        if (Boolean.TRUE.equals(onlyNotSold)) {
            clause = clause.and(onlyNotSoldPredicate());
        }

        return clause;
    }

    private BooleanExpression categoryPredicate(Long categoryId) {
        return PRODUCT.categoryId.eq(categoryId != null ? categoryId : DEFAULT_CATEGORY_ID);
    }

    private BooleanExpression endAtPredicate(LocalDateTime endAtFrom) {
        return AUCTION.endAt.goe(endAtFrom);
    }

    private BooleanExpression onlyNotSoldPredicate() {
        return AUCTION.status.ne(Status.FINISHED);
    }

    /**
     * Builds the select query with ordering and pagination applied.
     */
    private JPAQuery<SearchProductProjection> buildSelectQuery(BooleanExpression whereClause, String sortBy,
        Pageable pageable) {
        return jpaQueryFactory
            .select(Projections.constructor(SearchProductProjection.class,
                PRODUCT.image,
                PRODUCT.name,
                PRODUCT.registeredAt,
                AUCTION.endAt,
                AUCTION.currentPrice,
                AUCTION.minPrice,
                AUCTION.status.eq(Status.FINISHED)))
            .from(PRODUCT)
            .join(AUCTION).on(PRODUCT.id.eq(AUCTION.productId))
            .where(whereClause)
            .orderBy(
                new CaseBuilder()
                    .when(AUCTION.status.eq(Status.FINISHED)).then(1)
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
            .join(AUCTION).on(PRODUCT.id.eq(AUCTION.productId))
            .where(whereClause);
    }
}
