package org.oneog.uppick.auction.domain.product.common.exception;

import org.oneog.uppick.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품이 존재하지 않습니다."),
	CANNOT_READ_PRODUCT_INFO(HttpStatus.BAD_REQUEST, "해당 상품의 정보를 조회할 수 없습니다."),
	CANNOT_READ_PRODUCT_SIMPLE_INFO(HttpStatus.BAD_REQUEST, "해당 상품의 낙찰 정보를 조회할 수 없습니다."),
	AUCTION_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "경매 등록에 실패했습니다."),
	NOT_ON_SALE_PRODUCT(HttpStatus.BAD_REQUEST, "현재 판매중인 상품이 아닙니다."),

	//S3 이미지 파일
	EMPTY_FILE(HttpStatus.BAD_REQUEST, "파일이 비어있습니다"),
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 10MB를 초과했습니다"),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다"),
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다");

	private final HttpStatus status;
	private final String message;

}
