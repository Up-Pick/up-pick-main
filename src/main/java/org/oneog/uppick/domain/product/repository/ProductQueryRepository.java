package org.oneog.uppick.domain.product.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.category.entity.QCategory.*;
import static org.oneog.uppick.domain.member.entity.QMember.*;
import static org.oneog.uppick.domain.member.entity.QPurchaseDetail.*;
import static org.oneog.uppick.domain.member.entity.QSellDetail.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;

import java.util.List;
import java.util.Optional;

import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
					auction.minPrice,
					auction.currentPrice,
					auction.endAt,
					member.nickname))
			.from(product)
			.join(category).on(product.categoryId.eq(category.id))
			.join(auction).on(product.id.eq(auction.productId))
			.join(member).on(product.registerId.eq(member.id))
			.leftJoin(sellDetail).on(product.id.eq(sellDetail.productId))
			.where(productId != null ? product.id.eq(productId) : null)
			.fetchOne();

		return Optional.ofNullable(qResponse);
	}

	public Optional<ProductSimpleInfoResponse> getProductSimpleInfoById(Long productId) {

		return Optional.ofNullable(
			queryFactory
				.select(
					Projections.constructor(
						ProductSimpleInfoResponse.class,
						product.name,
						product.image,
						auction.minPrice,
						auction.currentPrice))
				.from(product)
				.join(auction).on(product.id.eq(auction.productId))
				.where(productId != null ? product.id.eq(productId) : null)
				.fetchOne());
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
					sellDetail.sellAt))
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
				.fetchOne()).orElse(0L);

		return new PageImpl<>(qResponseList, pageable, total);
	}

	public Page<ProductPurchasedInfoResponse> getPurchasedProductInfoByMemberId(Long memberId, Pageable pageable) {

		List<ProductPurchasedInfoResponse> qResponseList = queryFactory
			.select(
				Projections.constructor(
					ProductPurchasedInfoResponse.class,
					product.id,
					product.name,
					product.image,
					purchaseDetail.purchasePrice,
					purchaseDetail.purchaseAt))
			.from(product)
			.join(purchaseDetail).on(purchaseDetail.productId.eq(product.id))
			.where(memberId != null ? purchaseDetail.buyerId.eq(memberId) : null)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(
			queryFactory
				.select(product.count())
				.from(product)
				.join(purchaseDetail).on(purchaseDetail.productId.eq(product.id))
				.where(memberId != null ? purchaseDetail.buyerId.eq(memberId) : null)
				.fetchOne()).orElse(0L);

		return new PageImpl<>(qResponseList, pageable, total);
	}
}
