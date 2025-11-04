package org.oneog.uppick.domain.searching.command.service;

import java.util.List;

import org.oneog.uppick.domain.searching.command.entity.SearchHistory;
import org.oneog.uppick.domain.searching.command.repository.SearchHistoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchingCommandService {

	private final SearchHistoryJpaRepository repository;

	@Transactional
	public void saveSearchingHistories(List<String> keywords) {

		log.info("SearchingService - 검색 키워드 저장 시도 ⏳");

		keywords.forEach(keyword -> {
			SearchHistory history = new SearchHistory(keyword);
			repository.save(history);
		});

		log.info("SearchingService - 검색 키워드 저장 성공 ✅");
	}

}