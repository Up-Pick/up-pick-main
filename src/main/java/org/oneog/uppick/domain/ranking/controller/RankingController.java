package org.oneog.uppick.domain.ranking.controller;

import org.oneog.uppick.domain.ranking.service.RankingInternalService;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RankingController {

	private final RankingInternalService rankingInternalService;
}
