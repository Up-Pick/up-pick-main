package org.oneog.uppick.auction.domain.category.query.controller;

import java.util.List;

import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryResponse;
import org.oneog.uppick.auction.domain.category.query.service.CategoryQueryService;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryQueryController {

	private final CategoryQueryService categoryQueryService;

	@GetMapping
	public GlobalApiResponse<List<CategoryResponse>> getAllCategories() {

		List<CategoryResponse> allCategories = categoryQueryService.getAllCategories();
		return GlobalApiResponse.ok(allCategories);
	}

}
