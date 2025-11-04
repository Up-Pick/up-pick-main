package org.oneog.uppick.auction.domain.product.query.service;

import java.util.List;

import org.oneog.uppick.auction.domain.searching.service.SearchingInnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

	private final SearchingInnerService searchingInnerService;

	/**
	 * 검색 히스토리 저장 (별도 트랜잭션)
	 * - 검색 실패와 독립적으로 동작
	 * - MASTER DB 사용
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveSearchHistory(String keyword) {
		String[] splitKeywords = keyword.trim().split(" ");
		List<String> keywords = List.of(splitKeywords);
		searchingInnerService.saveSearchHistories(keywords);
	}

}
