package org.oneog.uppick.auction.domain.auction.query.repository;

import static org.oneog.uppick.auction.domain.product.entity.QProduct.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

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

}
