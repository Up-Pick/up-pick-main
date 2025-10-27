package org.oneog.uppick.auction.domain.category.controller;

import java.util.List;

import org.oneog.uppick.auction.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.auction.domain.category.service.CategoryInternalService;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
public class CategoryController {

	private final CategoryInternalService categoryInternalService;

	@GetMapping
	public GlobalApiResponse<List<CategoryResponse>> getAllCategories() {

		List<CategoryResponse> allCategories = categoryInternalService.getAllCategories();
		return GlobalApiResponse.ok(allCategories);
	}

}
