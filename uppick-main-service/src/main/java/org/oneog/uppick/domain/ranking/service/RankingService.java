package org.oneog.uppick.domain.ranking.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

	private final HotKeywordRepository hotKeywordRepository;

	//핫 키워드 TOP10 조회
	public List<HotKeywordResponse> getHotKeywords() {

		List<HotKeyword> hotKeywords = hotKeywordRepository.findAllByOrderByRankNoAsc();
		return toHotKeywordResponseList(hotKeywords);
	}

	private HotKeywordResponse toResponse(HotKeyword hotKeyword) {

		return new HotKeywordResponse(hotKeyword.getKeyword(), hotKeyword.getRankNo());
	}

	private List<HotKeywordResponse> toHotKeywordResponseList(List<HotKeyword> hotKeywords) {

		return hotKeywords.stream()
			.map(this::toResponse)
			.toList();
	}

}