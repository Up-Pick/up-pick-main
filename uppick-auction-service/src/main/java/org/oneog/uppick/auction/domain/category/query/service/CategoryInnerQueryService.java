package org.oneog.uppick.auction.domain.category.query.service;

import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryInfoResponse;

public interface CategoryInnerQueryService {

	CategoryInfoResponse getCategoriesByCategoryId(Long categoryId);

}
