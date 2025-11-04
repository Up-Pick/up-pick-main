package org.oneog.uppick.domain.searching.command.controller;

import org.oneog.uppick.domain.searching.command.dto.SaveSearchHistoriesRequest;
import org.oneog.uppick.domain.searching.command.service.SearchingCommandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/searching")
public class SearchingInternalCommandController {

	private final SearchingCommandService searchingService;

	@PostMapping
	public void saveSearchingHistories(@RequestBody SaveSearchHistoriesRequest request) {

		searchingService.saveSearchingHistories(request.getKeywords());
	}

}
