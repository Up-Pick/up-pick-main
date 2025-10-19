package org.oneog.uppick.domain.product.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.auction.entity.QBiddingDetail.*;
import static org.oneog.uppick.domain.category.entity.QCategory.*;
import static org.oneog.uppick.domain.member.entity.QMember.*;
import static org.oneog.uppick.domain.member.entity.QPurchaseDetail.*;
import static org.oneog.uppick.domain.member.entity.QSellDetail.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;
import static org.oneog.uppick.domain.product.entity.QProductViewHistory.*;

import java.util.List;
import java.util.Optional;

import org.oneog.uppick.domain.auction.entity.QAuction;
import org.oneog.uppick.domain.auction.entity.QBiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductRecentViewInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.entity.QProductViewHistory;
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
			.orderBy(purchaseDetail.purchaseAt.desc())
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
			.join(auction).on(auction.productId.eq(product.id))
			.join(biddingDetail).on(biddingDetail.auctionId.eq(auction.id))
			.where(
				biddingDetail.memberId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS))
					.and(biddingDetail.bidAt.eq(
						JPAExpressions.select(biddingDetailSub.bidAt.max())
							.from(biddingDetailSub)
							.join(auctionSub).on(biddingDetailSub.auctionId.eq(auctionSub.id))
							.where(
								biddingDetailSub.memberId.eq(memberId)
									.and(auctionSub.productId.eq(product.id))
							)
					))
			)
			.orderBy(biddingDetail.bidAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(auction.countDistinct())
			.from(auction)
			.join(biddingDetail).on(biddingDetail.auctionId.eq(auction.id))
			.where(
				biddingDetail.memberId.eq(memberId)
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
			.join(auction).on(auction.productId.eq(product.id))
			.where(
				product.registerId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS))
			)
			.orderBy(product.registeredAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(auction.count())
			.from(product)
			.join(auction).on(auction.productId.eq(product.id))
			.where(
				product.registerId.eq(memberId)
					.and(auction.status.eq(AuctionStatus.IN_PROGRESS)))
			.fetchOne()).orElse(0L);

		return new PageImpl<>(qResponses, pageable, total);
	}

	public Page<ProductRecentViewInfoResponse> getRecentViewProductInfoByMemberId(Long memberId, Pageable pageable) {

		QProductViewHistory productViewHistorySub = new QProductViewHistory("productViewHistorySub");

		List<ProductRecentViewInfoResponse> content = queryFactory
			.select(
				Projections.constructor(
					ProductRecentViewInfoResponse.class,
					product.id,
					product.name,
					product.image,
					auction.currentPrice,
					auction.endAt,
					productViewHistory.viewedAt))
			.from(product)
			.join(auction).on(auction.productId.eq(product.id))
			.join(productViewHistory).on(productViewHistory.productId.eq(product.id)
				.and(productViewHistory.viewedAt.eq(
					JPAExpressions
						.select(productViewHistorySub.viewedAt.max())
						.from(productViewHistorySub)
						.where(productViewHistorySub.productId.eq(product.id)
							.and(productViewHistorySub.memberId.eq(memberId)))
				)))
			.where(productViewHistory.memberId.eq(memberId)
				.and(auction.status.ne(AuctionStatus.EXPIRED)))
			.orderBy(productViewHistory.viewedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(product.countDistinct())
			.from(product)
			.join(productViewHistory).on(productViewHistory.productId.eq(product.id))
			.where(productViewHistory.memberId.eq(memberId))
			.fetchOne()).orElse(0L);

		return new PageImpl<>(content, pageable, total);
	}
}
