package org.oneog.uppick.domain.auction.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auction-client", url = "${internal.auction-service.url}")
public interface AuctionClient {

	@GetMapping("/internal/v1/biddings/members/ids")
	List<Long> getBiddingMemberIds(@RequestParam long auctionId, @RequestParam long excludeMemberId);

}
