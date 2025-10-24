package org.oneog.uppick.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPurchaseInfoWithoutBuyerRequest {
	private final Long id;
	private final String name;
	private final String image;
}