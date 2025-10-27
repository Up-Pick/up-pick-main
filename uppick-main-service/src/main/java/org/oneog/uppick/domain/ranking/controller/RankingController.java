package org.oneog.uppick.domain.ranking.controller;

import java.util.List;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

	private final RankingService rankingService;

	@GetMapping("search-keywords")
	public GlobalApiResponse<List<HotKeywordResponse>> getHotKeywords() {

		List<HotKeywordResponse> responses = rankingService.getHotKeywords();
		return GlobalApiResponse.ok(responses);
	}

}