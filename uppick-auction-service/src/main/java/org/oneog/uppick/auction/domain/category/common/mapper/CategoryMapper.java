package org.oneog.uppick.auction.domain.category.common.mapper;

import org.oneog.uppick.auction.domain.category.query.entity.Category;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

	public CategoryInfoResponse toCategoryInfoResponse(Category category) {

		return new CategoryInfoResponse(category.getBig(), category.getSmall());
	}

	public CategoryResponse toResponse(Category category) {

		return CategoryResponse.builder()
			.categoryId(category.getId())
			.smallCategory(category.getSmall())
			.bigCategory(category.getBig())
			.build();
	}

}
