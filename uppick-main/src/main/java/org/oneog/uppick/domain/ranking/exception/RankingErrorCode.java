package org.oneog.uppick.domain.ranking.exception;

import org.oneog.uppickcommon.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankingErrorCode implements ErrorCode {
	;

	private final HttpStatus status;
	private final String message;
}
