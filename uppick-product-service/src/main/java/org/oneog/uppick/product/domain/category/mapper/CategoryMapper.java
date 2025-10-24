package org.oneog.uppick.product.domain.category.mapper;

import org.oneog.uppick.product.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.product.domain.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

	public CategoryInfoResponse toCategoryInfoResponse(Category category) {
		return new CategoryInfoResponse(category.getBig(), category.getSmall());
	}
}
