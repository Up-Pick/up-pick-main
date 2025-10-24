package org.oneog.uppick.domain.searching.controller;

import org.oneog.uppick.domain.searching.dto.SaveSearchHistoriesRequest;
import org.oneog.uppick.domain.searching.service.SearchingInternalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/searching")
public class SearchingInternalController {

	private final SearchingInternalService searchingInternalService;

	@PostMapping
	public void saveSearchingHistories(@RequestBody SaveSearchHistoriesRequest request) {
		searchingInternalService.saveSearchingHistories(request.getKeywords());
	}
}
