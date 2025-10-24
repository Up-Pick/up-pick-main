package org.oneog.uppick.domain.searching.service;

import java.util.List;

import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.oneog.uppick.domain.searching.repository.SearchHistoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchingInternalService {

	private final SearchHistoryJpaRepository repository;

	@Transactional
	public void saveSearchingHistories(List<String> keywords) {
		keywords.forEach(keyword -> {
			SearchHistory history = new SearchHistory(keyword);
			repository.save(history);
		});
	}
}
