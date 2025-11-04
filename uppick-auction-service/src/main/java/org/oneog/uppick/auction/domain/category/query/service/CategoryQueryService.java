package org.oneog.uppick.auction.domain.category.query.service;

import java.util.List;

import org.oneog.uppick.auction.domain.category.common.mapper.CategoryMapper;
import org.oneog.uppick.auction.domain.category.query.entity.Category;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryResponse;
import org.oneog.uppick.auction.domain.category.query.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategories() {

		List<Category> categories = categoryRepository.findAll();
		return categories.stream().map(categoryMapper::toResponse).toList();
	}

}
