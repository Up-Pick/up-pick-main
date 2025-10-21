package org.oneog.uppick.domain.auction.exception;

import org.oneog.uppickcommon.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {

	WRONG_BIDDING_PRICE(HttpStatus.BAD_REQUEST, "입찰가를 잘못 입력하셨습니다."),
	AUCTION_FOUND_FOUND(HttpStatus.NOT_FOUND, "해당 경매가 존재하지 않습니다."),
	CANNOT_BID_OWN_AUCTION(HttpStatus.BAD_REQUEST, "본인의 판매 물품에는 입찰할 수 없습니다."),
	INSUFFICIENT_CREDIT(HttpStatus.BAD_REQUEST, "보유한 크레딧보다 큰 금액을 입력할수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
