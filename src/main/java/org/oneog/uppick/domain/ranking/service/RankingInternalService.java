package org.oneog.uppick.domain.ranking.service;

import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.RankingQueryRepository;
import org.oneog.uppick.domain.ranking.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingInternalService {

	private final HotDealRepository hotDealRepository;
	private final SearchHistoryRepository searchHistoryRepository;
	private final RankingQueryRepository rankingQueryRepository;

}
