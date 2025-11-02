package org.oneog.uppick.domain.ranking.controller;

import org.oneog.uppick.domain.ranking.service.RankingSchedulerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/rankings")
public class RankingInternalController {

	private final RankingSchedulerService rankingSchedulerService;

	@PostMapping("/update-hot-keywords")
	public void updateHotKeywords() {

		rankingSchedulerService.updateWeeklyTop10HotKeywords();
	}

}
