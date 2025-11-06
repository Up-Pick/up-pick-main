package org.oneog.uppick.domain.ranking.query.controller;

import java.util.List;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.ranking.query.model.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.query.service.RankingQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingQueryController {

	private final RankingQueryService rankingQueryService;

	@GetMapping("search-keywords")
	public GlobalApiResponse<List<HotKeywordResponse>> getHotKeywords() {

		List<HotKeywordResponse> responses = rankingQueryService.getHotKeywords();
		return GlobalApiResponse.ok(responses);
	}

}
