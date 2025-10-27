package org.oneog.uppick.product.domain.category.service;

import java.util.ArrayList;
import java.util.List;

import org.oneog.uppick.product.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.product.domain.category.entity.Category;
import org.oneog.uppick.product.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryInternalService {

	private final CategoryRepository categoryRepository;

	public List<CategoryResponse> getAllcategories() {
		List<Category> categories = categoryRepository.findAll();
		List<CategoryResponse> responses = new ArrayList<>();

		for (Category category : categories) {
			CategoryResponse response = new CategoryResponse(
				category.getId(),
				category.getBig(),
				category.getSmall());
			responses.add(response);
		}
		return responses;
	}
}
