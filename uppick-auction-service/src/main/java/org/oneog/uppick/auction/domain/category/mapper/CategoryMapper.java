package org.oneog.uppick.auction.domain.category.mapper;

import org.oneog.uppick.auction.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.auction.domain.category.entity.Category;
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
