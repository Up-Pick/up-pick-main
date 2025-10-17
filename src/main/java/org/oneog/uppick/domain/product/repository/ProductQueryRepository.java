package org.oneog.uppick.domain.product.repository;

import java.util.List;
import java.util.Optional;

import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import static org.oneog.uppick.domain.product.entity.QProduct.product;
import static org.oneog.uppick.domain.category.entity.QCategory.category;
import static org.oneog.uppick.domain.auction.entity.QAuction.auction;
import static org.oneog.uppick.domain.member.entity.QMember.member;
import static org.oneog.uppick.domain.product.entity.QSellDetail.sellDetail;


import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

	private final JPAQueryFactory queryFactory;

	public Optional<ProductInfoResponse> getProductInfoById(Long productId) {

		ProductInfoResponse qResponse = queryFactory
			.select(
				Projections.constructor(
					ProductInfoResponse.class,
					product.id,
					product.name,
					product.description,
					product.viewCount,
					product.registeredAt,
					product.image,
					Expressions.stringTemplate("CONCAT({0}, '/', {1})", category.big, category.small),
					product.soldAt,
					auction.currentPrice,
					auction.endAt,
					member.nickname
				)
			)
			.from(product)
			.join(category).on(product.categoryId.eq(category.id))
			.join(auction).on(product.id.eq(auction.productId))
			.join(member).on(product.registerId.eq(member.id))
			.leftJoin(sellDetail).on(product.id.eq(sellDetail.productId))
			.where(productId != null ? product.id.eq(productId) : null)
			.fetchOne();

		return Optional.ofNullable(qResponse);
	}

	public Page<ProductSoldInfoResponse> getProductSoldInfoByMemberId(Long memberId, Pageable pageable) {

		List<ProductSoldInfoResponse> qResponseList = queryFactory
			.select(
				Projections.constructor(
					ProductSoldInfoResponse.class,
					product.id,
					product.name,
					product.description,
					product.image,
					sellDetail.finalPrice,
					sellDetail.sellAt
				)
			)
			.from(product)
			.join(sellDetail).on(product.id.eq(sellDetail.productId))
			.where(memberId != null ? product.registerId.eq(memberId) : null)
			.orderBy(sellDetail.sellAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(
				queryFactory
					.select(product.count())
					.from(product)
					.join(sellDetail).on(product.id.eq(sellDetail.productId))
					.where(memberId != null ? product.registerId.eq(memberId) : null)
					.fetchOne()
		).orElse(0L);

		return new PageImpl<>(qResponseList, pageable, total);
	}

	public ProductSimpleInfoResponse getProductSimpleInfoById(Long productId) {

		return queryFactory
			.select(
				Projections.constructor(
					ProductSimpleInfoResponse.class,
					product.name,
					product.image,
					Expressions.cases()
						.when(auction.currentPrice.isNotNull())
						.then(auction.currentPrice)
						.otherwise(auction.minPrice)
				)
			)
			.from(product)
			.join(auction).on(product.id.eq(auction.productId))
			.where(productId != null ? product.id.eq(productId) : null)
			.fetchOne();
	}
}
