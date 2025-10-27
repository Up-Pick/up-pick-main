package org.oneog.uppick.auction.domain.category.service;

import org.oneog.uppick.auction.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.entity.Category;
import org.oneog.uppick.auction.domain.category.exception.CategoryErrorCode;
import org.oneog.uppick.auction.domain.category.mapper.CategoryMapper;
import org.oneog.uppick.auction.domain.category.repository.CategoryRepository;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultCategoryInnerService implements CategoryInnerService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Override
	public CategoryInfoResponse getCategoriesByCategoryId(Long categoryId) {

		Category category = findCategoryByIdOrElseThrow(categoryId);
		return categoryMapper.toCategoryInfoResponse(category);
	}

	private Category findCategoryByIdOrElseThrow(Long categoryId) {

		return categoryRepository.findById(categoryId)
			.orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
	}

}
