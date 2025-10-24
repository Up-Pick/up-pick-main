package org.oneog.uppick.domain.ranking.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.mapper.RankingMapper;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;

@ExtendWith(MockitoExtension.class)
class RankingInternalServiceTest {

	@Mock
	private HotKeywordRepository hotKeywordRepository;

	@Mock
	private RankingMapper rankingMapper;

	@InjectMocks
	private RankingInternalService rankingInternalService;

	// === 주간 키워드 조회 ===
	@Test
	void getHotKeywords_조회성공_순위오름차순으로조회된다() {
		// given
		HotKeyword keyword1 = createHotKeyword(1, "맥북");
		HotKeyword keyword2 = createHotKeyword(2, "아이폰");
		HotKeyword keyword3 = createHotKeyword(3, "에어팟");

		List<HotKeyword> keywords = List.of(keyword1, keyword2, keyword3);

		HotKeywordResponse response1 = new HotKeywordResponse("맥북", 1);
		HotKeywordResponse response2 = new HotKeywordResponse("아이폰", 2);
		HotKeywordResponse response3 = new HotKeywordResponse("에어팟", 3);
		List<HotKeywordResponse> responses = List.of(response1, response2, response3);

		given(hotKeywordRepository.findAllByOrderByRankNoAsc()).willReturn(keywords);
		given(rankingMapper.toHotKeywordResponseList(keywords)).willReturn(responses);

		// when
		List<HotKeywordResponse> result = rankingInternalService.getHotKeywords();

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getRankNo()).isEqualTo(1);
		assertThat(result.get(0).getKeyword()).isEqualTo("맥북");
		assertThat(result.get(1).getRankNo()).isEqualTo(2);
		assertThat(result.get(2).getRankNo()).isEqualTo(3);

		// 검증
		verify(hotKeywordRepository, times(1)).findAllByOrderByRankNoAsc();
		verify(rankingMapper, times(1)).toHotKeywordResponseList(keywords);
	}

	@Test
	void getHotKeywords_키워드없음_빈리스트반환된다() {
		// given
		given(hotKeywordRepository.findAllByOrderByRankNoAsc()).willReturn(Collections.emptyList());

		// when
		List<HotKeywordResponse> result = rankingInternalService.getHotKeywords();

		// then
		assertThat(result).isEmpty();
		verify(hotKeywordRepository, times(1)).findAllByOrderByRankNoAsc();
	}

	// === Helper Methods ===

	private HotKeyword createHotKeyword(int rankNo, String keyword) {
		return new HotKeyword(keyword, rankNo);
	}
}