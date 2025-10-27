package org.oneog.uppick.product.domain.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {
	private Long categoryId;
	private String bigCategory;
	private String smallCategory;
}
