package org.oneog.uppick.domain.ranking.query.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.command.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.command.repository.HotKeywordRepository;
import org.oneog.uppick.domain.ranking.query.model.dto.response.HotKeywordResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingQueryService {

	private final HotKeywordRepository hotKeywordRepository;

	//핫 키워드 TOP10 조회
	@Cacheable(value = "hotKeywords", key = "'top10'")
	public List<HotKeywordResponse> getHotKeywords() {

		log.debug("RankingQueryService - 핫 키워드 TOP10 조회 시도 ⏳");

		List<HotKeyword> hotKeywords = hotKeywordRepository.findAllByOrderByRankNoAsc();
		List<HotKeywordResponse> responses = toHotKeywordResponseList(hotKeywords);

		log.debug("RankingQueryService - 핫 키워드 TOP10 조회 성공 ✅");

		return responses;
	}

	private List<HotKeywordResponse> toHotKeywordResponseList(List<HotKeyword> hotKeywords) {

		return hotKeywords.stream()
			.map(keyword -> new HotKeywordResponse(keyword.getKeyword(), keyword.getRankNo()))
			.toList();
	}

}
