package org.oneog.uppick.product.domain.category.service;

import org.oneog.uppick.product.domain.category.dto.response.CategoryInfoResponse;

public interface CategoryExternalServiceApi {
	CategoryInfoResponse getCategoriesByCategoryId(Long categoryId);
}
