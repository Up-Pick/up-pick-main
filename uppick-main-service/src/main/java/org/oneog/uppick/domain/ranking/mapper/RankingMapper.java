package org.oneog.uppick.domain.ranking.mapper;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

	// === 핫키워드 ===
	public HotKeywordResponse toResponse(HotKeyword hotKeyword) {
		return new HotKeywordResponse(hotKeyword.getKeyword(), hotKeyword.getRankNo());
	}

	public List<HotKeywordResponse> toHotKeywordResponseList(List<HotKeyword> hotKeywords) {
		return hotKeywords.stream()
			.map(this::toResponse)
			.toList();
	}

}
