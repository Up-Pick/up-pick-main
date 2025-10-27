package org.oneog.uppick.product.domain.auction.service;

import java.time.LocalDateTime;

import org.oneog.uppick.product.domain.auction.entity.Auction;
import org.oneog.uppick.product.domain.auction.entity.AuctionStatus;
import org.oneog.uppick.product.domain.auction.repository.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionExternalServiceProvider implements AuctionExternalService {
	private final AuctionRepository auctionRepository;

	@Override
	@Transactional
	public void registerAuction(Long id, Long registerId, Long startBid, LocalDateTime registeredAt,
		LocalDateTime endAt) {
		Auction auction = Auction.builder()
			.productId(id)
			.registerId(registerId)
			.minPrice(startBid)
			.startAt(registeredAt)
			.endAt(endAt)
			.status(AuctionStatus.IN_PROGRESS)
			.build();
		auctionRepository.save(auction);
	}
}
