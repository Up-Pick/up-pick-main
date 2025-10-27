package org.oneog.uppick.auction.domain.category.service;

import org.oneog.uppick.auction.domain.category.dto.response.CategoryInfoResponse;

public interface CategoryInnerService {

	CategoryInfoResponse getCategoriesByCategoryId(Long categoryId);

}
