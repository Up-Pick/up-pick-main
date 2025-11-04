package org.oneog.uppick.auction.domain.category.query.service;

import org.oneog.uppick.auction.domain.category.common.exception.CategoryErrorCode;
import org.oneog.uppick.auction.domain.category.common.mapper.CategoryMapper;
import org.oneog.uppick.auction.domain.category.query.entity.Category;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.query.repository.CategoryRepository;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
