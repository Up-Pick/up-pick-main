package org.oneog.uppick.domain.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotDealResponse {

	private final Integer rankNo;
	private final Long productId;
	private final String productName;
	private final String productImage;

}
