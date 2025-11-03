package org.oneog.uppick.domain.ranking.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

	private final HotKeywordRepository hotKeywordRepository;

	//핫 키워드 TOP10 조회
	public List<HotKeywordResponse> getHotKeywords() {

		log.info("RankingService - 핫 키워드 TOP10 조회 시도 ⏳");

		List<HotKeyword> hotKeywords = hotKeywordRepository.findAllByOrderByRankNoAsc();

		log.info("RankingService - 핫 키워드 TOP10 조회 성공 ✅");

		return toHotKeywordResponseList(hotKeywords);
	}

	private List<HotKeywordResponse> toHotKeywordResponseList(List<HotKeyword> hotKeywords) {

		return hotKeywords.stream()
			.map(keyword -> new HotKeywordResponse(keyword.getKeyword(), keyword.getRankNo()))
			.toList();
	}

}