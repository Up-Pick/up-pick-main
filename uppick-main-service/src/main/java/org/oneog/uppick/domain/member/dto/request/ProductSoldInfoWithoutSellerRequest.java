package org.oneog.uppick.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSoldInfoWithoutSellerRequest {
	private final Long id;
	private final String name;
	private final String description;
	private final String image;
}
