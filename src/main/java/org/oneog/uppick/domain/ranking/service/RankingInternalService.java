package org.oneog.uppick.domain.ranking.service;

import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.oneog.uppick.domain.ranking.repository.RankingQueryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingInternalService {

	private final HotDealRepository hotDealRepository;
	private final HotKeywordRepository hotKeywordRepository;
	private final RankingQueryRepository rankingQueryRepository;

}
