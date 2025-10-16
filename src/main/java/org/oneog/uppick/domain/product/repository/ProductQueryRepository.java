package org.oneog.uppick.domain.product.repository;

import java.util.Optional;

import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
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
					category.big,
					sellDetail.sellAt,
					auction.currentPrice,
					auction.endAt,
					member.name
				)
			)
			.from(product)
			.join(category).on(product.categoryId.eq(category.id))
			.join(auction).on(product.id.eq(auction.productId))
			.join(member).on(product.registerId.eq(member.id))
			.leftJoin(sellDetail).on(product.id.eq(sellDetail.productId))
			.where(product.id.eq(productId))
			.fetchOne();

		return Optional.ofNullable(qResponse);
	}

}
