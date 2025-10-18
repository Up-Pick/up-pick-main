package org.oneog.uppick.domain.ranking.mapper;

import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
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

	public HotKeywordResponse toResponse(HotKeyword hotKeyword) {
		return new HotKeywordResponse(hotKeyword.getKeyword(), hotKeyword.getRankNo());
	}

}
