package org.oneog.uppick.domain.ranking.service;

import org.oneog.uppick.domain.ranking.repository.HotDealQueryRepository;
import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.SearchHistoryQueryRepository;
import org.oneog.uppick.domain.ranking.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingInternalService {

	private final HotDealRepository hotDealRepository;
	private final SearchHistoryRepository searchHistoryRepository;
	private final HotDealQueryRepository hotDealQueryRepository;
	private final SearchHistoryQueryRepository searchHistoryQueryRepository;

}
