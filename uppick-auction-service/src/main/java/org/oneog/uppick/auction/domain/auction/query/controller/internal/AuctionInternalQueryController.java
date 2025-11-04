package org.oneog.uppick.auction.domain.auction.query.controller.internal;

import java.util.List;

import org.oneog.uppick.auction.domain.auction.query.service.AuctionInternalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class AuctionInternalQueryController {

	private final AuctionInternalQueryService auctionInternalQueryService;

	@GetMapping("/biddings/members/ids")
	public List<Long> getBiddingMemberIds(@RequestParam long auctionId, @RequestParam long excludeMemberId) {

		return auctionInternalQueryService.fetchBidderIdsWithoutMember(auctionId, excludeMemberId);
	}

}