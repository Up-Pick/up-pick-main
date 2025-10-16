package org.oneog.uppick.domain.product.exception;

import org.oneog.uppick.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품이 존재하지 않습니다."),
	CANNOT_READ_PRODUCT_INFO(HttpStatus.BAD_REQUEST, "해당 상품을 조회할 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
