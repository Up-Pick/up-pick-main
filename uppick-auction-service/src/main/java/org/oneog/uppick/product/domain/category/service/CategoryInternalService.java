package org.oneog.uppick.product.domain.category.service;

import java.util.List;

import org.oneog.uppick.product.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.product.domain.category.entity.Category;
import org.oneog.uppick.product.domain.category.mapper.CategoryMapper;
import org.oneog.uppick.product.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryInternalService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	public List<CategoryResponse> getAllCategories() {

		List<Category> categories = categoryRepository.findAll();
		return categories.stream().map(categoryMapper::toResponse).toList();
	}

}
