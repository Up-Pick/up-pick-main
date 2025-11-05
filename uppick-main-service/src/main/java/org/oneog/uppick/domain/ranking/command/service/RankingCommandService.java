package org.oneog.uppick.domain.ranking.command.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.command.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.command.model.dto.projection.HotKeywordProjection;
import org.oneog.uppick.domain.ranking.command.repository.HotKeywordRepository;
import org.oneog.uppick.domain.ranking.query.repository.RankingQueryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCommandService {

	private final HotKeywordRepository hotKeywordRepository;
	private final RankingQueryRepository rankingQueryRepository;

	@Transactional
	@CacheEvict(value = "hotKeywords", allEntries = true)
	public void updateWeeklyTop10HotKeywords() {

		log.info("RankingCommandService - [Batch] 키워드 랭킹 업데이트 시작 ⏳");

		//이전 내용 삭제
		hotKeywordRepository.deleteAll();

		//핫 키워드 랭킹 가져오기(키워드, 검색 횟수)
		List<HotKeywordProjection> rankings = rankingQueryRepository.findTop10HotKeywordsByCount();

		//핫 키워드 랭킹 rankNo 설정 및 저장
		for (int i = 0; i < rankings.size(); i++) {
			HotKeywordProjection dto = rankings.get(i);
			HotKeyword hotKeyword = new HotKeyword(dto.getKeyword(), i + 1);
			hotKeywordRepository.save(hotKeyword);
		}

		log.info("RankingCommandService - [Batch] 키워드 랭킹 업데이트 종료 ✅");
	}

}
