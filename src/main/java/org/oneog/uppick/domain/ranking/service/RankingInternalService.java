package org.oneog.uppick.domain.ranking.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.mapper.RankingMapper;
import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingInternalService {

	private final HotDealRepository hotDealRepository;
	private final HotKeywordRepository hotKeywordRepository;
	private final RankingMapper rankingMapper;

	//핫딜 TOP6 상품 조회
	public List<HotDealResponse> getHotDeals() {
		return hotDealRepository
			.findAllByOrderByRankNoAsc()
			.stream()
			.map(rankingMapper::toResponse)
			.toList();
	}

	//핫 키워드 TOP10 조회
	public List<HotKeywordResponse> getHotKeywords() {
		return hotKeywordRepository
			.findAllByOrderByRankNoAsc()
			.stream()
			.map(rankingMapper::toResponse)
			.toList();
	}
}
