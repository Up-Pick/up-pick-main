package org.oneog.uppick.domain.ranking.mapper;

import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

	public HotDealResponse toResponse(HotDeal hotDeal) {
		return HotDealResponse.builder()
			.rank(hotDeal.getRankNo())
			.productId(hotDeal.getProductId())
			.productName(hotDeal.getProductName())
			.productImage(hotDeal.getProductImage())
			.build();
	}

}
