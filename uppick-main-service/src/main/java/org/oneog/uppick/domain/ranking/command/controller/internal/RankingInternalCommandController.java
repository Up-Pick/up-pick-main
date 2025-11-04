package org.oneog.uppick.domain.ranking.command.controller.internal;

import org.oneog.uppick.domain.ranking.command.service.RankingCommandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/rankings")
public class RankingInternalCommandController {

	private final RankingCommandService rankingCommandService;

	@PostMapping("/update-hot-keywords")
	public void updateHotKeywords() {

		rankingCommandService.updateWeeklyTop10HotKeywords();
	}

}
