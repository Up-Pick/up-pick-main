package org.oneog.uppick.domain.auction.repository;

import static org.oneog.uppick.domain.auction.entity.QAuction.*;
import static org.oneog.uppick.domain.member.entity.QMember.*;
import static org.oneog.uppick.domain.product.entity.QProduct.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Long findSellerIdByAuctionId(Long auctionId) {
		return jpaQueryFactory
			.select(product.registerId)
			.from(auction)
			.join(product).on(auction.productId.eq(product.id))
			.where(auction.id.eq(auctionId))
			.fetchOne();
	}

	/**
	 * 상품 ID로 상품 이름 조회
	 */
	public String findProductNameByProductId(Long productId) {
		return jpaQueryFactory
			.select(product.name)
			.from(product)
			.where(product.id.eq(productId))
			.fetchOne();
	}

	public Long findPointByMemberId(Long memberId) {
		return jpaQueryFactory
			.select(member.credit)
			.from(member)
			.where(member.id.eq(memberId))
			.fetchOne();
	}
}
