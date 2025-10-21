package org.oneog.uppick.domain.auction.service;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionExternalService implements AuctionExternalServiceApi {

	private final AuctionRepository auctionRepository;
	private final AuctionMapper auctionMapper;

	@Transactional
	public void registerAuction(Long productId, Long minPrice, LocalDateTime registerdAt, LocalDateTime endAt) {
		Auction auction = auctionMapper.registerToEntity(productId, minPrice, registerdAt, endAt);
		auctionRepository.save(auction);
	}

}
