package org.oneog.uppick.auction.domain.auction.controller;

import java.util.List;

import org.oneog.uppick.auction.domain.auction.service.AuctionInternalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class AuctionInternalController {

    private final AuctionInternalService auctionInternalService;

    @GetMapping("/biddings/members/ids")
    public List<Long> getBiddingMemberIds(@RequestParam long auctionId, @RequestParam long excludeMemberId) {

        return auctionInternalService.fetchBidderIdsWithoutMember(auctionId, excludeMemberId);
    }

}
