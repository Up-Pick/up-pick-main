package org.oneog.uppick.auction.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.auction.command.entity.Auction;
import org.oneog.uppick.auction.domain.auction.command.entity.AuctionStatus;
import org.oneog.uppick.auction.domain.auction.command.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.command.model.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.command.model.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRedisRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.command.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.auction.command.service.component.BiddingProcessor;
import org.oneog.uppick.auction.domain.auction.common.exception.AuctionErrorCode;
import org.oneog.uppick.auction.domain.auction.common.mapper.AuctionMapper;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BiddingProcessorTest {

	@Mock
	private AuctionRepository auctionRepository;
	@Mock
	private AuctionMapper auctionMapper;
	@Mock
	private BiddingDetailRepository biddingDetailRepository;
	@Mock
	private AuctionRedisRepository auctionRedisRepository;
	@Mock
	private MemberInnerService memberInnerService;

	@InjectMocks
	private BiddingProcessor biddingProcessor;

	// no notifications sent by BiddingProcessor currently

	@Test
	@DisplayName("입찰 시도 성공")
	void process_입찰시도_성공() {

		// given
		long auctionId = 1L;
		long memberId = 100L; // 입찰자
		long sellerId = 200L; // 판매자
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(sellerId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		ReflectionTestUtils.setField(auction, "id", auctionId);

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(memberInnerService.getMemberCredit(anyLong())).willReturn(999999L);

		// Redis에 현재 입찰가/마지막 입찰자 없음
		given(auctionRedisRepository.findCurrentBidPrice(auctionId)).willReturn(null);
		given(auctionRedisRepository.findLastBidderId(auctionId)).willReturn(null);

		willDoNothing().given(memberInnerService).updateMemberCredit(anyLong(), anyLong());

		BiddingDetail biddingDetail = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(memberId)
			.bidPrice(newBidPrice)
			.build();

		given(auctionMapper.toEntity(auctionId, memberId, newBidPrice))
			.willReturn(biddingDetail);

		// when
		BiddingResultDto result = biddingProcessor.process(request, auctionId, memberId);

		// then: 결과 DTO 검증
		assertThat(result).isNotNull();
		assertThat(result.getSellerId()).isEqualTo(sellerId);
		assertThat(result.getBiddingPrice()).isEqualTo(newBidPrice);

		// DB에 입찰 내역 저장 호출
		then(biddingDetailRepository).should().save(biddingDetail);

		// 크레딧이 차감 호출
		then(memberInnerService).should().updateMemberCredit(memberId, -newBidPrice);

		// Redis 상태 갱신 호출
		then(auctionRedisRepository).should().updateBidStatus(auctionId, newBidPrice, memberId);
	}

	@Test
	@DisplayName("잘못된 입찰가로 입찰 실패")
	void process_잘못된입찰가_실패() {

		// given
		long auctionId = 1L;
		long memberId = 100L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(2000L)
			.registerId(10L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(1500L); // 현재입찰가(2000)보다 낮음

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(memberInnerService.getMemberCredit(anyLong())).willReturn(999999L);

		// 현재 입찰가가 이미 2000
		given(auctionRedisRepository.findCurrentBidPrice(auctionId)).willReturn(2000L);

		// when/then
		assertThatThrownBy(() -> biddingProcessor.process(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessage(AuctionErrorCode.WRONG_BIDDING_PRICE.getMessage());
	}

	@Test
	void process_판매자_본인품목본인이입찰하는경우_예외() {

		// given
		long auctionId = 1L;
		long memberId = 100L; // 입찰자 = 판매자
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(memberId)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

		// when & then
		assertThatThrownBy(() -> biddingProcessor.process(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessage(AuctionErrorCode.CANNOT_BID_OWN_AUCTION.getMessage());

		then(biddingDetailRepository).should(never()).save(any());
		then(memberInnerService).should(never()).updateMemberCredit(anyLong(), anyLong());
	}

	@Test
	@DisplayName("크레딧 부족 예외")
	void process_크레딧부족_예외() {

		// given
		long auctionId = 1L;
		long memberId = 100L;
		long newBidPrice = 2000L;

		Auction auction = Auction.builder()
			.productId(10L)
			.minPrice(1000L)
			.currentPrice(1500L)
			.registerId(10L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(1))
			.build();

		AuctionBidRequest request = new AuctionBidRequest(newBidPrice);

		given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));
		given(memberInnerService.getMemberCredit(anyLong())).willReturn(1000L);

		// when & then
		assertThatThrownBy(() -> biddingProcessor.process(request, auctionId, memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(AuctionErrorCode.INSUFFICIENT_CREDIT.getMessage());

		then(biddingDetailRepository).should(never()).save(any());
	}

}
