package org.oneog.uppick.domain.auction.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionQueryRepository;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionInternalService {

	// ***** Auction Domain ***** //
	private final AuctionRepository auctionRepository;
	private final AuctionQueryRepository auctionQueryRepository;
	private final AuctionMapper auctionMapper;

	private final BiddingDetailRepository biddingDetailRepository;

	//특정 상품에 입찰 시도를 한다
	@Transactional
	public void bid(@Valid AuctionBidRequest request, long auctionId, long memberId) {

		Auction auction = findAuctionById(auctionId);

		Long biddingPrice = request.getBiddingPrice(); //입찰 금액
		Long currentPrice = auction.getCurrentPrice(); //현재 입찰가
		Long minPrice = auction.getMinPrice(); //최소 입찰가

		// 1. 첫 입찰일 경우 최소 입찰가보다는 커야한다.
		// 2. 상품의 현재 입찰가가 null 이거나(현재 아무도 입찰하지 않음) 유저의 입찰금액이 더 커야함
		boolean validBid =
			(currentPrice == null && biddingPrice >= minPrice) ||
				(currentPrice != null && biddingPrice > currentPrice);

		if (validBid) {
			//성공 로직
			// 입찰 시 경매의 현재 입찰가가 갱신되어야한다.
			auction.updateCurrentPrice(request.getBiddingPrice());

			BiddingDetail biddingDetail = auctionMapper.toEntity(
				auctionId,
				memberId,
				request.getBiddingPrice()
			);
			// 입찰 시 입찰 내역에 기록이 남아야한다.
			biddingDetailRepository.save(biddingDetail);
		} else {
			//입찰 실패 로직
			throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
		}

	}

	private Auction findAuctionById(long auctionId) {
		return auctionRepository.findById(auctionId)
			.orElseThrow(() -> new BusinessException(AuctionErrorCode.AUCTION_FOUND_FOUND));
	}

}
