package org.oneog.uppick.domain.ranking.controller;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.service.RankingInternalService;
import org.oneog.uppickcommon.common.dto.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

	private final RankingInternalService rankingInternalService;

	@GetMapping("/hot-deals")
	public GlobalApiResponse<List<HotDealResponse>> getHotDeals() {
		List<HotDealResponse> responses = rankingInternalService.getHotDeals();
		return GlobalApiResponse.ok(responses);
	}

	@GetMapping("search-keywords")
	public GlobalApiResponse<List<HotKeywordResponse>> getHotKeywords() {
		List<HotKeywordResponse> responses = rankingInternalService.getHotKeywords();
		return GlobalApiResponse.ok(responses);
	}
}