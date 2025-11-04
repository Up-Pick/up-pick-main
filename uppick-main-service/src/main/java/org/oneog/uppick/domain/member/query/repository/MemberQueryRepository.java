package org.oneog.uppick.domain.member.query.repository;

import static org.oneog.uppick.domain.member.command.entity.QPurchaseDetail.*;
import static org.oneog.uppick.domain.member.command.entity.QSellDetail.*;

import java.util.List;

import org.oneog.uppick.domain.member.query.model.dto.response.PurchasedProductBuyAtResponse;
import org.oneog.uppick.domain.member.query.model.dto.response.SoldProductSellAtResponse;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<SoldProductSellAtResponse> findSellAtByProductIds(List<Long> productIds) {

		return jpaQueryFactory
			.select(
				Projections.constructor(
					SoldProductSellAtResponse.class,
					sellDetail.productId,
					sellDetail.sellAt))
			.from(sellDetail)
			.where(sellDetail.productId.in(productIds))
			.orderBy(sellDetail.sellAt.desc())
			.fetch();
	}

	public List<PurchasedProductBuyAtResponse> findBuyAtByProductIds(List<Long> productIds) {

		return jpaQueryFactory
			.select(
				Projections.constructor(
					PurchasedProductBuyAtResponse.class,
					purchaseDetail.productId,
					purchaseDetail.purchaseAt))
			.from(purchaseDetail)
			.where(purchaseDetail.productId.in(productIds))
			.orderBy(purchaseDetail.purchaseAt.desc())
			.fetch();
	}

}
