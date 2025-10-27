package org.oneog.uppick.auction.domain.searching.client;

import org.oneog.uppick.auction.domain.searching.dto.SaveSearchHistoriesRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "searching-client", url = "${gateway.url}")
public interface SearchingClient {

	@PostMapping("/main/internal/v1/searching")
	void saveSearchHistory(@RequestBody SaveSearchHistoriesRequest keyword);

}
