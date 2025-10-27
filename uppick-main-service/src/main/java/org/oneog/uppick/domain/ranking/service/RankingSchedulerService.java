package org.oneog.uppick.domain.ranking.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.HotKeywordCalculationDto;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.oneog.uppick.domain.ranking.repository.RankingQueryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSchedulerService {

	private final HotKeywordRepository hotKeywordRepository;
	private final RankingQueryRepository rankingQueryRepository;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정 업데이트
	@Transactional
	public void updateWeeklyTop10HotKeywords() {

		log.info("[Scheduler] 키워드 랭킹 업데이트 시작");

		//이전 내용 삭제
		hotKeywordRepository.deleteAll();

		//핫 키워드 랭킹 가져오기(키워드, 검색 횟수)
		List<HotKeywordCalculationDto> rankings = rankingQueryRepository.findTop10HotKeywordsByCount();

		//핫 키워드 랭킹 rankNo 설정 및 저장
		for (int i = 0; i < rankings.size(); i++) {
			HotKeywordCalculationDto dto = rankings.get(i);
			HotKeyword hotKeyword = new HotKeyword(dto.getKeyword(), i + 1);
			hotKeywordRepository.save(hotKeyword);
		}

		log.info("[Scheduler] 키워드 랭킹 업데이트 종료");
	}

}