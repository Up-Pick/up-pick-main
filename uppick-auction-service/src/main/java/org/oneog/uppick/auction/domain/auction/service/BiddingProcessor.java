package org.oneog.uppick.auction.domain.auction.service;

import org.oneog.uppick.auction.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.auction.domain.auction.dto.request.BiddingResultDto;
import org.oneog.uppick.auction.domain.auction.entity.Auction;
import org.oneog.uppick.auction.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.auction.domain.auction.exception.AuctionErrorCode;
import org.oneog.uppick.auction.domain.auction.mapper.AuctionMapper;
import org.oneog.uppick.auction.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.auction.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.auction.domain.member.service.MemberInnerService;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiddingProcessor {

    private final AuctionRepository auctionRepository;
    private final MemberInnerService memberInnerService;
    private final AuctionMapper auctionMapper;
    private final BiddingDetailRepository biddingDetailRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BiddingResultDto process(@Valid AuctionBidRequest request, long auctionId, long memberId) {

        try {
            Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(AuctionErrorCode.AUCTION_NOT_FOUND));

            // 판매자 본인 입찰 방지
            if (auction.getRegisterId().equals(memberId)) {
                throw new BusinessException(AuctionErrorCode.CANNOT_BID_OWN_AUCTION);
            }

            Long biddingPrice = request.getBiddingPrice();
            Long currentPrice = auction.getCurrentPrice();
            Long minPrice = auction.getMinPrice();

            // 포인트 잔액 확인
            Long memberCredit = memberInnerService.getMemberCredit(memberId);
            if (biddingPrice > memberCredit) {
                throw new BusinessException(AuctionErrorCode.INSUFFICIENT_CREDIT);
            }

            // 입찰 가능 여부 확인
            boolean validBid = (currentPrice == null && biddingPrice >= minPrice) ||
                (currentPrice != null && biddingPrice > currentPrice);

            if (!validBid) {
                throw new BusinessException(AuctionErrorCode.WRONG_BIDDING_PRICE);
            }

            // 이전 최고 입찰자 조회
            Long previousBidderId = auction.getLastBidderId();
            Long previousBidPrice = currentPrice;

            //  본인 재입찰 or 신규 입찰 처리
            if (previousBidderId != null && previousBidderId.equals(memberId) && previousBidPrice != null) {
                // 본인 재입찰: 차액만 차감
                long additionalAmount = biddingPrice - previousBidPrice;
                memberInnerService.updateMemberCredit(memberId, -additionalAmount);
                log.info("기존 입찰자({}) 재입찰: 추가 차감 {}", memberId, additionalAmount);
            } else {
                // 새 입찰자: 전체 금액 차감
                memberInnerService.updateMemberCredit(memberId, -biddingPrice);

                // 이전 최고 입찰자 환불
                if (previousBidderId != null && previousBidPrice != null) {
                    memberInnerService.updateMemberCredit(previousBidderId, previousBidPrice);
                    log.info("이전 최고 입찰자({}) 환불: {}", previousBidderId, previousBidPrice);
                }
            }

            // 입찰가 갱신 및 기록 저장
            auction.updateCurrentPrice(biddingPrice, memberId);

            BiddingDetail biddingDetail = auctionMapper.toEntity(
                auctionId,
                memberId,
                biddingPrice);
            biddingDetailRepository.save(biddingDetail);

            return new BiddingResultDto(auction.getRegisterId(), biddingPrice);

        } catch (Exception e) {
            log.error("입찰 처리 중 예외 발생", e);
            throw e;
        }
    }

}
