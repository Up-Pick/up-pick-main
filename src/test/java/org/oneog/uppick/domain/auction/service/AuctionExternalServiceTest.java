package org.oneog.uppick.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;

@ExtendWith(MockitoExtension.class)
public class AuctionExternalServiceTest {

	@Mock
	private AuctionRepository auctionRepository;

	@Mock
	private AuctionMapper auctionMapper;

	@InjectMocks
	private AuctionExternalService auctionExternalService;

	@Test
	void registerAuction_정상적인요청_Auction에_저장됨() {

		// given 상품에서 받아올 정보들
		Long productId = 1L;
		Long minPrice = 1000L;
		LocalDateTime endAt = LocalDateTime.now().plusDays(1);  //임의로 +1 로 설정

		Auction auction = Auction.builder()
			.productId(productId)
			.currentPrice(null)
			.minPrice(minPrice)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(endAt)
			.build();

		given(auctionMapper.registerToEntity(productId, minPrice, null, endAt)).willReturn(auction);

		// when
		auctionExternalService.registerAuction(productId, minPrice, null, endAt); //내가 테스트 하려는거

		// then
		ArgumentCaptor<Auction> captor = ArgumentCaptor.forClass(Auction.class); //Auction 객체를 캡쳐할 준비

		verify(auctionRepository).save(captor.capture()); // save()가 호출되었는지 검증하면서 캡쳐
		Auction saved = captor.getValue(); //캡쳐한거 saved로 담기

		assertThat(saved.getProductId()).isEqualTo(1L);
		assertThat(saved.getMinPrice()).isEqualTo(1000L);
	}
}
