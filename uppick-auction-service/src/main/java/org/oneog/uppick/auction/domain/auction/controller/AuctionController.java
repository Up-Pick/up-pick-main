package org.oneog.uppick.auction.domain.auction.controller;

import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.service.AuctionService;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionController {

	private final AuctionService auctionService;

	// 경매 올라온 상품 입찰하기
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{auctionId}/bid")
	public GlobalApiResponse<Void> auctionBid(@Valid @RequestBody AuctionBidRequest request,
		@PathVariable long auctionId, @AuthenticationPrincipal AuthMember authMember) {

		auctionService.bid(request, auctionId, authMember.getMemberId());
		return GlobalApiResponse.ok(null);
	}

}
