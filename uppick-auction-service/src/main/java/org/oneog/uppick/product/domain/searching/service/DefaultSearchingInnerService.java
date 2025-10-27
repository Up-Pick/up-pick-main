package org.oneog.uppick.product.domain.searching.service;

import java.util.List;

import org.oneog.uppick.product.domain.searching.client.SearchingClient;
import org.oneog.uppick.product.domain.searching.dto.SaveSearchHistoriesRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultSearchingInnerService implements SearchingInnerService {

	private final SearchingClient searchingClient;

	@Override
	public void saveSearchHistories(List<String> keywords) {

		SaveSearchHistoriesRequest request = new SaveSearchHistoriesRequest(keywords);
		searchingClient.saveSearchHistory(request);
	}

}
