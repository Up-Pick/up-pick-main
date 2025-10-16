package org.oneog.uppick.domain.bid.exception;

import org.oneog.uppick.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BidErrorCode implements ErrorCode {

	private final HttpStatus status;
	private final String message;
}
