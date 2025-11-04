package org.oneog.uppick.auction.domain.product.repository;

import static org.oneog.uppick.auction.domain.auction.command.entity.QAuction.*;
import static org.oneog.uppick.auction.domain.auction.command.entity.QBiddingDetail.*;
import static org.oneog.uppick.auction.domain.category.query.entity.QCategory.*;
import static org.oneog.uppick.auction.domain.product.entity.QProduct.*;

import java.util.List;
import java.util.Optional;

import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.entity.QAuction;
import org.oneog.uppick.auction.domain.auction.command.entity.QBiddingDetail;
import org.oneog.uppick.auction.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.ProductSimpleInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductSellingInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

	private final JPAQueryFactory queryFactory;

	public Optional<ProductDetailProjection> getProductInfoById(Long productId) {

		ProductDetailProjection qResponse = queryFactory
			.select(
				Projections.constructor(
					ProductDetailProjection.class,
					product.id,
					product.name,
					product.description,
					product.viewCount,
					product.registeredAt,
					product.image,
					Expressions.stringTemplate("CONCAT({0}, '/', {1})", product.bigCategory, product.smallCategory),
					auction.minPrice,
					auction.endAt,
					auction.registerId,
					auction.id))
			.from(product)
			.join(category)
			.on(product.categoryId.eq(category.id))
			.join(auction)
			.on(auction.productId.eq(product.id))
			.where(productId != null ? product.id.eq(productId) : null)
			.fetchOne();

		return Optional.ofNullable(qResponse);
	}

	public Optional<ProductSimpleInfoProjection> getProductSimpleInfoById(Long productId) {

		return Optional.ofNullable(
			queryFactory
				.select(
					Projections.constructor(
						ProductSimpleInfoProjection.class,
						product.name,
						product.image,
						auction.minPrice,
						auction.id))
				.from(product)
				.join(auction)
				.on(product.id.eq(auction.productId))
				.where(productId != null ? product.id.eq(productId) : null)
				.fetchOne());
	}

	public Page<SoldProductInfoProjection> getProductSoldInfoByMemberId(Long memberId, Pageable pageable) {

		List<SoldProductInfoProjection> qResponseList = queryFactory
			.select(
				Projections.constructor(
					SoldProductInfoProjection.class,
					product.id,
					product.name,
					product.description,
					product.image,
					auction.currentPrice))
			.from(product)
			.join(auction)
			.on(auction.productId.eq(product.id))
			.where(memberId != null ? auction.registerId.eq(memberId)
				.and(auction.status.eq(AuctionStatus.FINISHED)) : null)
			.orderBy(auction.endAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(
				queryFactory
					.select(product.count())
					.from(product)
					.join(auction)
					.on(auction.productId.eq(product.id))
					.where(memberId != null ? auction.registerId.eq(memberId)
						.and(auction.status.eq(AuctionStatus.FINISHED)) : null)
					.fetchOne())
			.orElse(0L);

		return new PageImpl<>(qResponseList, pageable, total);
	}

	public Page<PurchasedProductInfoProjection> getPurchasedProductInfoByMemberId(Long memberId,
		Pageable pageable) {

		List<PurchasedProductInfoProjection> qResponseList = queryFactory
			.select(
				Projections.constructor(
					PurchasedProductInfoProjection.class,
					product.id,
					product.name,
					product.image,
					auction.currentPrice
				))
			.from(product)
			.join(auction)
			.on(auction.productId.eq(product.id))
			.where(memberId != null ? auction.lastBidderId.eq(memberId)
				.and(auction.status.eq(AuctionStatus.FINISHED)) : null)
			.orderBy(auction.endAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(
				queryFactory
					.select(product.count())
					.from(product)
					.join(auction)
					.on(auction.productId.eq(product.id))
					.where(memberId != null ? auction.lastBidderId.eq(memberId)
						.and(auction.status.eq(AuctionStatus.FINISHED)) : null)
					.fetchOne())
			.orElse(0L);

		return new PageImpl<>(qResponseList, pageable, total);
	}

	public Page<ProductBiddingInfoResponse> getBiddingProductInfoByMemberId(Long memberId, Pageable pageable) {

		QBiddingDetail biddingDetailSub = new QBiddingDetail("biddingDetailSub");
		QAuction auctionSub = new QAuction("auctionSub");

		List<ProductBiddingInfoResponse> qResponses = queryFactory
			.select(
				Projections.constructor(
					ProductBiddingInfoResponse.class,
					product.id,
					product.name,
					product.image,
					auction.endAt,
					auction.currentPrice,
					biddingDetail.bidPrice))
			.from(product)
			.join(auction)
			.on(auction.productId.eq(product.id))
			.join(biddingDetail)
			.on(biddingDetail.auctionId.eq(auction.id))
			.where(
				biddingDetail.bidderId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS))
					.and(biddingDetail.bidAt.eq(
						JPAExpressions.select(biddingDetailSub.bidAt.max())
							.from(biddingDetailSub)
							.join(auctionSub)
							.on(biddingDetailSub.auctionId.eq(auctionSub.id))
							.where(
								biddingDetailSub.bidderId.eq(memberId)
									.and(auctionSub.productId.eq(product.id))))))
			.orderBy(biddingDetail.bidAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(auction.countDistinct())
			.from(auction)
			.join(biddingDetail)
			.on(biddingDetail.auctionId.eq(auction.id))
			.where(
				biddingDetail.bidderId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS)))
			.fetchOne()).orElse(0L);

		return new PageImpl<>(qResponses, pageable, total);
	}

	public Page<ProductSellingInfoResponse> getSellingProductInfoMyMemberId(Long memberId, Pageable pageable) {

		List<ProductSellingInfoResponse> qResponses = queryFactory
			.select(
				Projections.constructor(
					ProductSellingInfoResponse.class,
					product.id,
					product.name,
					product.image,
					auction.endAt,
					auction.currentPrice,
					auction.id))
			.from(product)
			.join(auction)
			.on(auction.productId.eq(product.id))
			.where(
				product.registerId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS)))
			.orderBy(product.registeredAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(auction.count())
			.from(product)
			.join(auction)
			.on(auction.productId.eq(product.id))
			.where(
				product.registerId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS)))
			.fetchOne()).orElse(0L);

		return new PageImpl<>(qResponses, pageable, total);
	}

}
