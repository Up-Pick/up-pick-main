package org.oneog.uppick.auction.domain.auction.query.service;

import java.util.List;

import org.oneog.uppick.auction.domain.auction.query.repository.BiddingDetailQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionInternalQueryService {

	private final BiddingDetailQueryRepository biddingDetailQueryRepository;

	@Transactional(readOnly = true)
	public List<Long> fetchBidderIdsWithoutMember(long auctionId, long excludeMemberId) {

		return biddingDetailQueryRepository.findDistinctBidderIdsByAuctionExcludeMember(auctionId, excludeMemberId);
	}

}
